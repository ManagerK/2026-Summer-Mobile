오늘 학습한 내용 (2026-07-01)

요약
- findViewById 사용과 이름 일치의 중요성: 레이아웃에 정의된 id(visable_button)와 코드의 참조를 일치시켜 오류 해결.
- Kotlin에서 뷰 타입 import 추가: Button import 추가로 컴파일 오류 해결.
- ViewBinding 사용법 및 설정: 모듈(viewbindingexam)의 build.gradle.kts에 buildFeatures { viewBinding = true } 추가.
- ActivityMainBinding 사용: ActivityMainBinding.inflate(layoutInflater)로 바인딩 생성하고 setContentView(binding.root)로 설정.

수정한 파일
- visableclick/src/main/java/ai/hnu/kr/visableclick/MainActivity.kt (findViewById로 버튼 참조 수정, Button import 추가)
- viewbindingexam/build.gradle.kts (viewBinding 활성화)
- viewbindingexam/src/main/java/ai/hnu/kr/viewbindingexam/MainActivity.kt (ActivityMainBinding import 추가)

다음 단계
1. Android Studio에서 Gradle 동기화 및 빌드 수행 (바인딩 클래스 생성 확인)
2. 빌드 오류가 남으면 빌드 로그를 확인하고 오류 메시지를 공유하기

간단한 팁
- 레이아웃 id와 코드에서 사용하는 id는 정확히 일치해야 함.
- viewBinding 활성화 후에는 자동으로 생성되는 바인딩 클래스를 사용하면 findViewById가 필요 없음.

(자동 생성된 README)