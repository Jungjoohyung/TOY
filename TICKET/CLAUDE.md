# 프로젝트 설정 (로컬)

> 개인 작업 원칙과 하네스 5단계 요약은 글로벌 `~/.claude/CLAUDE.md` 참조.
> 자동화 개념(Worktree·Hook·Husky)이 처음이면 글로벌 `~/.claude/docs/automation-guide.md` 참조.

## 기술 스택
- 언어: <채워주세요>
- 프레임워크: <채워주세요>
- DB / 벡터DB: <채워주세요>
- 테스트 도구: <채워주세요>
- 린트 도구: <채워주세요>

## 에이전트 구성
- `agents/orchestrator.md` — 파이프라인 지휘자 (Opus)
- `agents/design.md` — 설계, Plan 단계 담당 (Opus)
- `agents/develop.md` — 구현 (Sonnet)
- `agents/test.md` — 테스트, Test 단계 담당 (Sonnet)
- `agents/verify.md` — 검증, Verify 단계 담당 (Opus)

각 에이전트 파일 frontmatter에 모델 명시됨.

## 에이전트 출력 형식
- 설계·검증: Markdown
- 테스트: JSON

## 하네스 5단계 - 프로젝트 상세

글로벌 CLAUDE.md의 5단계 요약에 대응하는 이 프로젝트의 구체적 경로·스크립트.

| 단계 | 담당 | 산출물 경로 | 실행 방법 |
|------|------|------------|-----------|
| 1. Plan | design_agent | `docs/plans/<기능명>.md` | orchestrator가 호출 |
| 2. Worktree | orchestrator | `../<프로젝트>-<기능명>/` 폴더 | `./scripts/new-worktree.sh <기능명>` |
| 3. Test | test_agent | `tests/test_<기능명>.py` | orchestrator가 호출 |
| 3'. Develop | develop_agent | `src/` 하위 소스 | 워크트리 내에서 작업 |
| 4. Verify | verify_agent + 스크립트 | `docs/verify/<기능명>.md` | `./scripts/verify.sh` |
| 5. Commit & Merge | orchestrator | 메인 브랜치 반영 | git commit → 훅 통과 → git merge |

## 컨텍스트 접근 규칙
- 세부 문서는 한 번에 전부 읽지 말 것
- `docs/INDEX.md`에서 목차 먼저 확인
- 현재 작업에 필요한 문서만 선택적으로 로드

## 강제 사항 (Git 훅으로 차단됨)

글로벌 금지 사항 외에, 이 프로젝트만의 Git 수준 강제 규칙.

- **커밋 차단 조건 1**: `./scripts/verify.sh` 실패 시
  - 실행 위치: `.husky/pre-commit`
- **커밋 차단 조건 2**: 메인/마스터 브랜치에서 직접 커밋 시도 시
  - 실행 위치: `.husky/pre-commit`
- **커밋 메시지 형식 강제**: 아래 접두사 필수
  - `feat:` 새 기능 / `fix:` 버그 수정 / `docs:` 문서
  - `test:` 테스트 / `refactor:` 리팩토링 / `chore:` 기타
  - 실행 위치: `.husky/commit-msg`

## 모델 설정 (참고)
- 글로벌 보조: `.claude/settings.json`에 `"model": "opusplan"` 설정됨
- 프로젝트별 에이전트 모델은 각 `agents/*.md` frontmatter에서 지정
