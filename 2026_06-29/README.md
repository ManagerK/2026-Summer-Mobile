# 학습 요약 — 2026-06-29

오늘 노트북: Array and For-Loops.ipynb

요약:

- 배열(Array, IntArray)
  - Array(size, init) / IntArray, arrayOf, intArrayOf
  - 람다 초기화(Array(5){ i -> i * 3 })와 크기(size), 인덱스 접근
  - 문자열 템플릿에서 배열 값 출력

- 반복문
  - for (item in array), for (i in array.indices)
  - withIndex()로 (index, value) 사용
  - 반복 변수는 직접 변경 불가(값 복사), 요소 수정은 인덱스로 수행

- 콜렉션
  - listOf: 순서 있음, 중복 허용
  - setOf: 순서 없음, 중복 불가
  - mapOf: key -> value, keys/values/entries 반복

- 클래스 및 객체지향(Kotlin)
  - 기본 생성자와 init 블록, 보조 생성자
  - constructor 매개변수에 val/var 지정 시 멤버 자동 선언
  - data class: 자동 equals, toString, copy 등
  - companion object: 클래스 이름으로 접근하는 정적 멤버
  - 상속: open, override로 멤버/메서드 재정의

예시 출력 요약:
- 배열/람다 초기화 출력: 0,3,6,9,12
- 반복문으로 요소 수정 후 출력: 10,20,...,90
- map 반복 예시: key/value 출력
- 클래스 예시: name: Hong / age: 30

참고: 원본 노트북 파일(`Array and For-Loops.ipynb`)에 코드와 실행 결과가 포함되어 있습니다.