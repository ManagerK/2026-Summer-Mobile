# class_and_functions 학습 요약 (2026-06-30)

학습일: 2026-06-30

요약
- Kotlin에서 클래스와 함수(특히 람다와 고차함수)를 실습함.
- 상속, 추상 클래스, 인터페이스, 클래스 위임(의존성 주입 형태) 및 람다 표현식의 사용법을 학습.

주요 내용

1) 클래스와 상속
- open 키워드로 상속을 허용하고, override로 메서드 재정의.
- 예: Shape, Circle, Rect, Ellipse 등. 리스트에 담아 forEach로 다형성 활용.
- is, as 연산자로 타입 검사와 캐스팅 가능.

2) 추상 클래스
- abstract class로 공통 인터페이스와 일부 구현(일반 메서드)을 정의.
- 예: Animal2는 abstract fun bark() 선언과 run(name:String) 구현을 함께 가짐.

3) 인터페이스
- interface로 기능 규약 정의, 기본 구현 가능.
- 예: Flyable(fly(), land() 기본 구현)와 Duck의 오버라이드.

4) 클래스 위임(의존성 주입 패턴)
- Printer 인터페이스와 ColorPrinter 구현체, 이를 주입받아 Logger2가 동작.
- 구성에 따라 출력 형식 변경 가능.

5) 람다와 고차함수
- 기본 람다: val double: (Int)->Int = { it * 2 }
- 고차함수: fun multiplier(factor:Int): (Int)->Int = { it * factor }
- 람다 인자 이름 생략(it)과 반환으로 다른 함수를 리턴하는 패턴 확인.
- 예: testFun(no, predicate) -> 결과에 따라 문자열을 반환하는 함수(함수를 반환).

실습 결과(일부)
- 다형성 출력: 원/사각형/타원형 그리기 출력 확인
- 추상/인터페이스 예제 출력: "야옹...", "오리가 날다" 등
- 람다 예제 출력: 8, "함수 동작 : 15", "testFun result: invalid"

핵심 포인트
- Kotlin의 클래스 계층 구조와 다형성 활용법을 익힘.
- 인터페이스와 추상 클래스의 차이(구현 강제 vs 일부 구현 제공)를 이해.
- 람다와 고차함수로 함수형 스타일을 사용하면 코드 재사용과 추상화가 쉬움.

다음 학습 계획(권장)
- 데이터 클래스와 sealed class 학습
- 컬렉션 함수(map/filter/reduce)와 람다 결합 실습
- Kotlin 코루틴의 기초 학습

작성자: 학습 노트 자동 요약
