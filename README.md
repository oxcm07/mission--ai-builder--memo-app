# Memo

Kotlin Multiplatform와 Compose Multiplatform Desktop으로 만든 로컬 파일 기반 메모 앱입니다.

## 기능

- 메모 목록, 제목/본문 편집, 새 메모 생성
- 제목과 본문 대상 실시간 검색
- 500ms debounce 자동 저장 및 `Ctrl/Cmd + S` 즉시 저장
- 삭제 전 확인 다이얼로그
- pinned 메모 상단 정렬
- 다크 모드 토글
- 본문 글자 수와 저장 상태 표시
- 앱 재실행 후에도 JSON 파일로 메모 유지

## 저장 위치

기본 저장 파일은 사용자 홈 디렉터리의 `.memo/notes.json`입니다.

저장 시 `notes.json.tmp`에 먼저 기록한 뒤 교체하며, 기존 파일은 가능한 경우 `notes.json.bak`로 백업합니다.
손상된 JSON 파일을 발견하면 백업 파일을 만든 뒤 빈 목록으로 시작합니다.

## 실행

macOS/Linux:

```shell
./gradlew run
```

Windows:

```shell
.\gradlew.bat run
```

## 테스트

```shell
./gradlew :composeApp:allTests
```

Windows:

```shell
.\gradlew.bat :composeApp:allTests
```

## 패키징

JDK 17 이상을 기준으로 현재 OS용 배포 파일을 만들 수 있습니다.

```shell
./gradlew packageDistributionForCurrentOS
```

Windows:

```shell
.\gradlew.bat packageDistributionForCurrentOS
```
