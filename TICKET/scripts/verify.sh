#!/usr/bin/env bash
# ============================================================
# verify.sh
# 커밋 전 자동 검증 스크립트 (린트 + 빌드 + 테스트)
# 사용법: ./scripts/verify.sh
# 이 스크립트는 .husky/pre-commit 에서 자동 호출됩니다.
# ============================================================

# 엄격 모드
set -euo pipefail

# 색상 코드 (터미널 출력용)
# \033[ 는 색상 시작, \033[0m 는 리셋
RED='\033[0;31m'      # 에러 표시용 빨강
GREEN='\033[0;32m'    # 성공 표시용 초록
YELLOW='\033[1;33m'   # 경고 표시용 노랑
NC='\033[0m'          # 색상 리셋 (No Color)

# 실패 카운트 변수
FAILED=0

echo "======================================"
echo "검증 시작 (Verify)"
echo "======================================"

# ----- 단계 1: 린트 검사 -----
# 린트(Lint)란? 코드 스타일·문법 오류를 자동으로 찾아주는 도구
# 프로젝트 스택에 따라 아래 명령을 수정하세요
echo ""
echo -e "${YELLOW}[1/3] 린트 검사${NC}"

if [ -f "package.json" ] && grep -q "\"lint\"" package.json; then
    # package.json 에 lint 스크립트가 정의된 경우 (JavaScript/TypeScript)
    if npm run lint; then
        echo -e "${GREEN}린트 통과${NC}"
    else
        echo -e "${RED}린트 실패${NC}"
        FAILED=$((FAILED + 1))
    fi
elif command -v ruff > /dev/null 2>&1; then
    # Python 프로젝트이고 ruff 가 설치된 경우
    if ruff check .; then
        echo -e "${GREEN}린트 통과${NC}"
    else
        echo -e "${RED}린트 실패${NC}"
        FAILED=$((FAILED + 1))
    fi
elif [ -f "gradlew" ]; then
    # Java/Gradle 프로젝트 - checkstyle 등 정적 분석
    if ./gradlew check -x test; then
        echo -e "${GREEN}린트 통과${NC}"
    else
        echo -e "${RED}린트 실패${NC}"
        FAILED=$((FAILED + 1))
    fi
else
    echo "린트 도구를 찾을 수 없음 (건너뜀)"
fi

# ----- 단계 2: 빌드 검사 -----
# 빌드란? 코드가 실제로 돌아가는 상태로 컴파일/번들링되는지 확인
echo ""
echo -e "${YELLOW}[2/3] 빌드 검사${NC}"

if [ -f "package.json" ] && grep -q "\"build\"" package.json; then
    # JavaScript/TypeScript 빌드
    if npm run build; then
        echo -e "${GREEN}빌드 통과${NC}"
    else
        echo -e "${RED}빌드 실패${NC}"
        FAILED=$((FAILED + 1))
    fi
elif [ -f "pyproject.toml" ] || [ -f "setup.py" ]; then
    # Python 프로젝트 - 문법 검사로 대체 (Python은 빌드 단계가 필수가 아님)
    if python -m compileall -q .; then
        echo -e "${GREEN}문법 검사 통과${NC}"
    else
        echo -e "${RED}문법 검사 실패${NC}"
        FAILED=$((FAILED + 1))
    fi
elif [ -f "gradlew" ]; then
    # Java/Gradle 프로젝트 빌드
    if ./gradlew build -x test; then
        echo -e "${GREEN}빌드 통과${NC}"
    else
        echo -e "${RED}빌드 실패${NC}"
        FAILED=$((FAILED + 1))
    fi
else
    echo "빌드 설정을 찾을 수 없음 (건너뜀)"
fi

# ----- 단계 3: 테스트 실행 -----
# 단위 테스트가 모두 통과하는지 확인
echo ""
echo -e "${YELLOW}[3/3] 테스트 실행${NC}"

if [ -f "package.json" ] && grep -q "\"test\"" package.json; then
    # JavaScript/TypeScript 테스트
    if npm test; then
        echo -e "${GREEN}테스트 통과${NC}"
    else
        echo -e "${RED}테스트 실패${NC}"
        FAILED=$((FAILED + 1))
    fi
elif command -v pytest > /dev/null 2>&1; then
    # Python pytest
    if pytest; then
        echo -e "${GREEN}테스트 통과${NC}"
    else
        echo -e "${RED}테스트 실패${NC}"
        FAILED=$((FAILED + 1))
    fi
elif [ -f "gradlew" ]; then
    # Java/Gradle 테스트
    if ./gradlew test; then
        echo -e "${GREEN}테스트 통과${NC}"
    else
        echo -e "${RED}테스트 실패${NC}"
        FAILED=$((FAILED + 1))
    fi
else
    echo "테스트 도구를 찾을 수 없음 (건너뜀)"
fi

# ----- 최종 결과 판정 -----
echo ""
echo "======================================"
if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}전체 검증 통과${NC}"
    echo "======================================"
    exit 0
else
    echo -e "${RED}검증 실패: ${FAILED}개 항목${NC}"
    echo "======================================"
    # exit 1 로 종료하면 pre-commit 훅이 커밋을 차단함
    exit 1
fi
