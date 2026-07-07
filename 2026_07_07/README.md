# Android Activity 화면 전환 학습 프로젝트

**학습 날짜:** 2026년 7월 7일

## 📚 학습 개요

이 프로젝트는 Android에서 여러 Activity 간의 화면 전환을 구현하는 방법을 학습하는 프로젝트입니다.
Intent를 사용하여 다양한 화면을 전환하고 Activity 간의 네비게이션을 관리하는 기능을 익혔습니다.

## 🎯 학습 목표

- Android Activity의 개념 이해
- Intent를 활용한 화면 전환 구현
- ViewBinding을 이용한 UI 요소 접근
- Kotlin을 이용한 Android 애플리케이션 개발
- Activity 간의 네비게이션 관리

## 📱 프로젝트 구조

```
2026_07_07/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/ai/hnu/kr/july07application/
│   │   │   │   ├── MainActivity.kt          # 메인 화면 (진입점)
│   │   │   │   ├── SecondActivity.kt        # 두 번째 화면
│   │   │   │   ├── ThirdActivity.kt         # 세 번째 화면
│   │   │   │   └── FourthActivity.kt        # 네 번째 화면
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── activity_second.xml
│   │   │   │   │   ├── activity_third.xml
│   │   │   │   │   └── activity_fourth.xml
│   │   │   │   └── values/
│   │   │   │       ├── strings.xml
│   │   │   │       ├── colors.xml
│   │   │   │       └── themes.xml
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   └── build.gradle.kts
└── build.gradle.kts
```

## 🔑 핵심 학습 내용

### 1. **Activity 생성 및 관리**
- 4개의 Activity 클래스 작성
- 각 Activity는 AppCompatActivity를 상속받아 구현
- onCreate() 메서드를 통한 레이아웃 설정

### 2. **Intent를 통한 화면 전환**
```kotlin
// 화면 전환 예제
binding.secondbtn.setOnClickListener {
    val intent = Intent(this, SecondActivity::class.java)
    startActivity(intent)
}
```

### 3. **ViewBinding 활용**
- ViewBinding을 통해 XML 레이아웃을 Kotlin 코드에서 타입 안전하게 접근
- 예: `ActivityMainBinding.inflate(layoutInflater)`
- 전통적인 findViewById() 사용 제거

### 4. **Activity Lifecycle 관리**
- finish() 메서드를 통한 Activity 종료
- 뒤로가기 버튼 구현 시 이전 Activity로 돌아가기

### 5. **UI 레이아웃 설계**
- ConstraintLayout을 이용한 반응형 레이아웃 설계
- LinearLayout을 이용한 버튼 배치
- TextView와 Button 등의 위젯 활용

## 💻 기술 스택

- **언어:** Kotlin
- **Framework:** Android (API 29 이상)
- **Build Tool:** Gradle
- **UI 바인딩:** ViewBinding
- **레이아웃:** ConstraintLayout, LinearLayout
- **IDE:** Android Studio

## 📋 구현된 기능

### MainActivity (메인 화면)
- "두번째화면", "세번째화면", "네번째 화면" 버튼 3개 배치
- 각 버튼 클릭 시 해당 Activity로 전환

### SecondActivity (두 번째 화면)
- "메인", "세번째화면", "네번째 화면" 버튼 배치
- 메인으로 돌아갈 때 finish() 호출

### ThirdActivity (세 번째 화면)
- "메인", "두번째화면", "네번째 화면" 버튼 배치
- 다른 화면으로 이동할 때 finish() 호출

### FourthActivity (네 번째 화면)
- "메인", "두번째화면", "세번째화면" 버튼 배치
- 모든 버튼 클릭 시 Activity 종료

## 🛠️ 개발 환경 설정

```
- compileSdk: 37
- minSdk: 29
- targetSdk: 37
- Java 호환성: VERSION_11
```

## 📚 학습 포인트

1. **Intent의 종류 이해**
   - 명시적 Intent (Explicit Intent): 특정 Activity로 직접 이동

2. **Activity Stack 관리**
   - 각 화면의 생명주기 이해
   - finish() 메서드로 스택에서 제거

3. **사용자 인터페이스 구성**
   - ConstraintLayout의 제약 조건 활용
   - LinearLayout의 weight를 이용한 균등 배분

4. **코드 간결성**
   - ViewBinding으로 null 안전성 확보
   - setOnClickListener를 통한 이벤트 처리

## ✅ 완성된 결과

- 4개의 Activity가 서로 네비게이션 가능한 구조 완성
- 모든 화면 전환 기능 정상 작동
- ViewBinding을 통한 안전한 UI 접근

## 🎓 다음 학습 주제 제안

- Activity 간 데이터 전달 (Intent extras)
- Fragment를 이용한 UI 구성
- RecyclerView를 이용한 리스트 표시
- SharedPreferences를 이용한 데이터 저장
- ViewModel과 LiveData를 이용한 MVVM 패턴

---

**프로젝트 완성 상태:** ✅ 학습 완료
**마지막 수정 일자:** 2026-07-07
