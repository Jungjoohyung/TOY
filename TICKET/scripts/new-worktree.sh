#!/usr/bin/env bash
# ============================================================
# new-worktree.sh
# 새 기능 개발용 Git Worktree를 생성하는 스크립트
# 사용법: ./scripts/new-worktree.sh <기능명>
# 예시:   ./scripts/new-worktree.sh login
# ============================================================

# 엄격 모드: 에러 발생 시 즉시 중단, 미정의 변수 사용 시 에러
set -euo pipefail

# ----- 1. 입력 인자 확인 -----
# 스크립트에 기능명이 전달되었는지 확인
if [ $# -lt 1 ]; then
    echo "에러: 기능명을 입력해주세요"
    echo "사용법: $0 <기능명>"
    echo "예시: $0 login"
    exit 1
fi

FEATURE_NAME="$1"                      # 예: login
BRANCH_NAME="feature/${FEATURE_NAME}"  # 브랜치명: feature/login
WORKTREE_PATH="../$(basename "$(pwd)")-${FEATURE_NAME}"
# 워크트리 경로: 상위 디렉토리에 "현재폴더명-기능명" 으로 생성
# 예: 현재 폴더가 "myproject"면 "../myproject-login" 생성

echo "======================================"
echo "워크트리 생성 시작"
echo "  기능명: ${FEATURE_NAME}"
echo "  브랜치: ${BRANCH_NAME}"
echo "  경로:   ${WORKTREE_PATH}"
echo "======================================"

# ----- 2. Git 저장소 여부 확인 -----
# 현재 폴더가 Git 저장소인지 확인
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo "에러: 현재 폴더는 Git 저장소가 아닙니다"
    echo "먼저 'git init' 을 실행하세요"
    exit 1
fi

# ----- 3. 동일 브랜치/워크트리 존재 여부 확인 -----
# 이미 같은 이름의 워크트리가 있으면 중복 생성 방지
if git worktree list | grep -q "${WORKTREE_PATH}"; then
    echo "에러: 이미 같은 경로에 워크트리가 존재합니다"
    echo "기존 워크트리 삭제 후 다시 시도하세요:"
    echo "  git worktree remove ${WORKTREE_PATH}"
    exit 1
fi

# ----- 4. 워크트리 생성 -----
# 새 브랜치 + 새 워크트리 폴더를 한 번에 생성
# -b 옵션은 새 브랜치를 생성한다는 의미
git worktree add -b "${BRANCH_NAME}" "${WORKTREE_PATH}"

echo ""
echo "워크트리 생성 완료"
echo ""
echo "다음 단계:"
echo "  1. cd ${WORKTREE_PATH}"
echo "  2. 의존성 설치 (예: npm install, pip install -r requirements.txt)"
echo "  3. 구현 시작"
echo ""
echo "작업 완료 후 메인 브랜치로 병합하려면:"
echo "  cd - (원래 폴더로 복귀)"
echo "  git checkout main"
echo "  git merge ${BRANCH_NAME}"
echo "  git worktree remove ${WORKTREE_PATH}"
