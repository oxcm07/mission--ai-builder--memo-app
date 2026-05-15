# Memo

Kotlin Multiplatform와 Compose Multiplatform Desktop으로 만든 로컬 파일 기반 메모 앱입니다.

## 주요 기능

- Apple Notes 스타일의 3단 레이아웃
  - 폴더 패널: 전체 메모와 사용자 폴더
  - 메모 목록
  - 편집 영역
- Windows 11 스타일 커스텀 타이틀바
  - 최소화, 최대화/복원, 닫기 버튼
  - 타이틀바 드래그로 창 이동
  - 다크 모드 전환 시 타이틀바도 다크 색상으로 전환
- 메모 생성, 수정, 삭제
  - 제목과 본문 입력
  - 폴더별 메모 분류
  - 메모를 폴더로 드래그앤드롭해 이동
  - 선택한 사용자 폴더를 상단 삭제 버튼으로 삭제
  - 메모 목록에서 드래그로 순서 변경
  - 제목이 비어 있으면 본문 첫 줄을 목록 제목처럼 표시
  - 삭제 전 확인 다이얼로그
- 자동 저장
  - 제목이나 본문 수정 후 debounce 자동 저장
  - `Ctrl/Cmd + S` 즉시 저장
  - 저장 실패 시 하단 상태 영역에 오류 표시
- 검색
  - 제목과 본문 대상 실시간 검색
  - 대소문자 구분 없이 검색
- 고정 메모
  - 메모 목록에서 우클릭 메뉴로 고정/고정 해제
  - 고정 메모는 일반 메모보다 우선 표시
- TXT 파일 가져오기
  - `TXT 가져오기` 버튼으로 하나 이상의 `.txt` 파일을 새 메모로 저장
  - 앱 창에 `.txt` 파일을 드래그앤드롭해 새 메모로 저장
  - UTF-8, UTF-16, CP949/MS949, EUC-KR, Shift-JIS 등 주요 텍스트 인코딩 자동 감지
- Sticky Notes 스타일 창
  - 선택한 메모를 `띄우기` 버튼으로 별도 작은 창에 표시
  - Sticky 창에서 수정한 제목/본문도 기존 메모와 함께 자동 저장
- 다크 모드
  - 테마 메뉴에서 시스템, 라이트, 다크 모드 선택
  - 시스템 모드에서는 OS 설정에 따라 자동 적용
  - 사이드바, 편집 영역, 상태바, 타이틀바가 함께 전환
- 기타
  - 하단 상태바에 저장 상태, 글자 수, 줄 수, 인코딩 표시
  - 폴더, 메모 목록, 편집 영역 폭 조절
  - Pretendard 기본 폰트와 시스템 설치 폰트 선택
  - 편집/미리보기 탭 제거 후 바로 편집하는 단일 편집기
  - 단축키: 새 메모, 저장, 검색 포커스, 삭제 요청

## 저장 방식

메모는 사용자 홈 디렉터리 아래 JSON 파일로 저장됩니다.

```text
~/.memo/notes.json
~/.memo/folders.json
```

저장 시 임시 파일에 먼저 기록한 뒤 교체하며, 기존 파일은 가능한 경우 백업합니다.

```text
~/.memo/notes.json.bak
~/.memo/folders.json.bak
```

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

macOS/Linux:

```shell
./gradlew :composeApp:allTests
```

Windows:

```shell
.\gradlew.bat :composeApp:allTests
```

## 빌드

```shell
./gradlew build
```

Windows:

```shell
.\gradlew.bat build
```

## 패키징

JDK 17 이상을 기준으로 현재 OS용 배포 파일을 만들 수 있습니다.

macOS/Linux:

```shell
./gradlew packageDistributionForCurrentOS
```

Windows:

```shell
.\gradlew.bat packageDistributionForCurrentOS
```

## 기술 스택

- Kotlin Multiplatform
- Compose Multiplatform for Desktop
- Gradle Kotlin DSL
- Kotlin Serialization
- kotlinx-datetime
- 로컬 JSON 파일 저장소
- 단순 MVVM 스타일 상태 관리
