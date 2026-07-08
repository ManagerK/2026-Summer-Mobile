# July08Application - Android 학습 프로젝트

**작성일**: 2026-07-08

## 📚 오늘의 학습 내용 요약

### 1. 계산기 앱 (Calculator App)
**위치**: `app/src/main/java/ai/hnu/kr/july08application/MainActivity.kt`

#### 주요 기능
- ✅ 두 개의 숫자 입력 기능
- ✅ 사칙연산 선택 (덧셈, 뺄셈, 곱셈, 나눗셈)
- ✅ 계산 결과 표시
- ✅ 예외 처리 (공백 입력, 유효하지 않은 숫자, 0으로 나누기)

#### 핵심 구현 사항
```kotlin
// EditText에서 사용자 입력 받기
val s1 = binding.editTextNumberDecimal.text?.toString()
val s2 = binding.editTextNumberDecimal2.text?.toString()

// RadioButton으로 연산자 선택
val result = when {
    binding.addado.isChecked -> n1 + n2
    binding.minusado.isChecked -> n1 - n2
    binding.multiado.isChecked -> n1 * n2
    binding.deviderdo.isChecked -> n1 / n2
}

// Toast를 사용한 사용자 알림
Toast.makeText(this, "메시지", Toast.LENGTH_SHORT).show()
```

#### UI 구성
- LinearLayout으로 수직 배치
- EditText: 두 개의 숫자 입력 필드
- RadioGroup: 사칙연산 선택 버튼
- Button: 계산 실행
- TextView: 결과 표시

---

### 2. TimePicker & DatePicker 다이얼로그 앱
**위치**: `picker/src/main/java/ai/hnu/kr/picker/MainActivity.kt`

#### 주요 기능
- ✅ TimePicker 다이얼로그 (24시간 형식)
- ✅ DatePicker 다이얼로그
- ✅ 시간 선택 2개 (시간1, 시간2)
- ✅ 날짜 선택 2개 (날짜1, 날짜2)
- ✅ 선택된 값을 TextView에 표시

#### 핵심 구현 사항
```kotlin
// TimePicker 다이얼로그
private fun showTimePicker(onTimeSet: (Int, Int) -> Unit) {
    val calendar = Calendar.getInstance()
    val timePickerDialog = TimePickerDialog(
        this,
        { _, selectedHour, selectedMinute ->
            onTimeSet(selectedHour, selectedMinute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true  // 24시간 형식
    )
    timePickerDialog.show()
}

// DatePicker 다이얼로그
private fun showDatePicker(onDateSet: (Int, Int, Int) -> Unit) {
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        this,
        { _, selectedYear, selectedMonth, selectedDay ->
            onDateSet(selectedYear, selectedMonth, selectedDay)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.show()
}
```

#### 출력 형식
- **시간**: HH:MM (예: 11:33)
- **날짜**: YYYY-MM-DD (예: 2026-07-08)

#### UI 구성
- TimePicker 섹션: 2개 버튼 + 2개 시간 표시
- DatePicker 섹션: 2개 버튼 + 2개 날짜 표시
- 구분된 섹션 제목 (한국어)

---

## 🛠️ 학습한 Android 개념

### 1. 데이터 바인딩 (Data Binding)
```kotlin
val binding = ActivityMainBinding.inflate(layoutInflater)
setContentView(binding.root)
```
- View를 직접 findViewById 없이 접근 가능
- 타입 안전성 증대

### 2. 대화상자 (Dialog)
- **TimePickerDialog**: 시간 선택
- **DatePickerDialog**: 날짜 선택
- 콜백 함수로 사용자 선택 처리

### 3. 예외 처리
```kotlin
if (s1.isNullOrBlank() || s2.isNullOrBlank()) {
    Toast.makeText(this, "숫자를 입력하세요", Toast.LENGTH_SHORT).show()
    return@setOnClickListener
}
```

### 4. Kotlin 기본 문법
- **let 연산자**: Null 안전 처리
- **when 표현식**: 조건부 분기
- **람다 함수**: 콜백 처리
- **String.format()**: 형식화된 문자열

### 5. Calendar API
```kotlin
val calendar = Calendar.getInstance()
val hour = calendar.get(Calendar.HOUR_OF_DAY)
val minute = calendar.get(Calendar.MINUTE)
val year = calendar.get(Calendar.YEAR)
```

---

## 📁 프로젝트 구조

```
July08Application/
├── app/                          # 계산기 모듈
│   └── src/main/
│       ├── java/.../MainActivity.kt
│       └── res/layout/activity_main.xml
│
├── picker/                       # TimePicker & DatePicker 모듈
│   └── src/main/
│       ├── java/.../MainActivity.kt
│       └── res/layout/activity_main.xml
│
└── README.md                     # 이 파일
```

---

## 🚀 빌드 & 실행

### 전체 프로젝트 빌드
```bash
./gradlew build
```

### 특정 모듈 빌드
```bash
./gradlew app:assembleDebug       # 계산기 앱
./gradlew picker:assembleDebug    # TimePicker/DatePicker 앱
```

### 실행
- Android Studio에서 Run 버튼 클릭
- 또는 `./gradlew installDebug`로 설치

---

## 💡 주요 학습 포인트

1. **UI 컴포넌트 활용**: Button, EditText, TextView, RadioButton, RadioGroup
2. **이벤트 처리**: 클릭 리스너, 콜백 함수
3. **다이얼로그 사용**: TimePickerDialog, DatePickerDialog
4. **데이터 검증**: Null 체크, 형식 검증
5. **사용자 알림**: Toast 메시지
6. **Kotlin 문법**: 람다, when, let 등

---

## 📝 참고사항

- 모든 코드는 Kotlin으로 작성됨
- Android API Level 21+ 호환
- Data Binding 활성화 상태
- 한국어 UI 지원

---

**작성자**: GitHub Copilot  
**마지막 수정**: 2026-07-08 11:33
