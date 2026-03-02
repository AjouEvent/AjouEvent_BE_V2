# Git Convention

## Branch Naming

```
feat/#[github issue number]/하고자하는 일 간략히
```

**예시:**
- `feat/#75/createProjectForm`

## Commit Message

```
{Type}: {Message}
```

**예시:**
- `feat: 예약 Dto 수정`

### Type

- `feat` : 새로운 기능 추가
- `fix` : 버그 수정
- `docs` : 문서 수정
- `style` : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
- `refactor` : 코드 리팩토링
- `test` : 테스트 코드, 리펙토링 테스트 코드 추가
- `chore` : 빌드 업무 수정, 패키지 매니저 수정
- `design` : (프론트) CSS 수정 및 CSS 추가 (기능 X)
- `remove` : 코드 / 파일을 삭제한 경우

### Message

- 한글로 작성
- 파일명, 디렉토리명 작성 금지

### Task Number

- 깃헹 이슈넘버 작성 (ex. #1)

### Etc.

- `:` 뒤에만 스페이스가 있음

## Pull Request

### Title

```
[Type] 한 일 #[github issue number]
```

**예시:**
- `[Infra] Vercel 배포 스크립트 및 Git Action 추가 #64`

## Merge Commit

### Format

```
[#{github issue number}] {taskTitle} (#{PR 번호})
```

**예시:**
- `[#171] Fix: 모바일 검색바 반영 높이 수정 (#173)`

### Merge 방식

- **Squash Merge** 사용

## Issue

### Title Format

```
[Type] 한 일
```

**예시:**
- `[Feat] 사용자 로그인 기능 추가`
- `[Bug] 키스톤 토큰 관련 버그 발견`
- `[Refactor] 코드 구조 개선`

