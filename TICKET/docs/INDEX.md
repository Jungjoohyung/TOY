# 문서 인덱스 (Context Index)

> 에이전트는 이 파일을 먼저 확인하고, 현재 작업에 필요한 문서만 선택적으로 로드합니다.
> 전체 문서를 한 번에 읽지 마세요. 토큰을 낭비하고 집중도가 떨어집니다.

---

## 1. 프로젝트 운영 문서

| 파일 | 역할 | 언제 읽어야 하나 |
|------|------|------------------|
| `CLAUDE.md` (로컬) | 프로젝트 스택·에이전트 구성·하네스 상세 경로 | 모든 세션 시작 시 (자동) |
| `~/.claude/CLAUDE.md` (글로벌) | 개인 작업 원칙·하네스 5단계 요약 | 모든 세션 시작 시 (자동) |
| `~/.claude/docs/automation-guide.md` (글로벌) | Worktree·Hook·Husky 개념 설명 | 자동화 환경 처음 세팅할 때 |

## 2. 에이전트 프롬프트

| 파일 | 역할 | 언제 읽어야 하나 |
|------|------|------------------|
| `agents/orchestrator.md` | 파이프라인 지휘자 | 전체 작업 시작할 때 |
| `agents/design.md` | 설계 담당 (Plan 단계) | Plan 단계 실행 시 |
| `agents/develop.md` | 구현 담당 | Worktree 내 구현 시 |
| `agents/test.md` | 테스트 담당 (Test 단계) | Test 단계 실행 시 |
| `agents/verify.md` | 검증 담당 (Verify 단계) | 검증 리포트 작성 시 |

## 3. 계획 문서 (Plan 단계 산출물)

각 기능 개발 시 생성되는 설계 문서입니다. `docs/plans/<기능명>.md` 형식.

| 파일 | 기능 | 상태 |
|------|------|------|
| (예시) `docs/plans/login.md` | 사용자 로그인 | 작성 중/완료 |
| `docs/plans/performance-list-with-status.md` | 예매 상태 포함 공연 목록 조회 API | 완료 |

> 이 목록은 새 기능 추가 시 orchestrator가 갱신합니다.

## 4. 검증 리포트 (Verify 단계 산출물)

각 기능의 검증 결과. `docs/verify/<기능명>.md` 형식.

| 파일 | 기능 | 판정 |
|------|------|------|
| (예시) `docs/verify/login.md` | 사용자 로그인 | APPROVED/REJECTED |

## 5. 자동화 스크립트

| 파일 | 역할 |
|------|------|
| `scripts/new-worktree.sh` | 새 기능용 Git Worktree 생성 |
| `scripts/verify.sh` | 린트·빌드·테스트 통합 검증 |

## 6. Git Hooks

| 파일 | 역할 | 언제 실행 |
|------|------|-----------|
| `.husky/pre-commit` | verify.sh 실행, 메인 브랜치 커밋 차단 | 커밋 직전 |
| `.husky/commit-msg` | 커밋 메시지 형식 검사 | 메시지 작성 직후 |

---

## 사용 예시

### 개발 에이전트가 로그인 기능을 구현할 때

1. 이 파일(INDEX.md)을 읽음
2. `CLAUDE.md`에서 전역 규칙 파악
3. `agents/develop.md`에서 자기 역할 파악
4. `docs/plans/login.md`에서 이번 작업의 설계 파악
5. `scripts/verify.sh`가 뭘 검사하는지만 확인 (내부 로직은 불필요)

→ 6개 파일만 읽고 끝. 전체 문서를 다 읽을 필요 없음.

### 오케스트레이터가 새 기능을 시작할 때

1. 이 파일을 읽음
2. `CLAUDE.md`로 프로젝트 상태 파악
3. 필요한 에이전트 프롬프트만 순서대로 호출
4. 작업 완료 후 이 파일에 `docs/plans/` `docs/verify/` 항목 추가
