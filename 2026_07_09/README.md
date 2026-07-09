README — 2026-07-09 학습 기록 및 변경사항 요약

개요
---
이 저장소에서 2026-07-09에 진행한 학습/개발 작업을 정리합니다. 목표는 기존 Fragment 기반 앱을 보존하면서 UI 동작을 개선하고, RecyclerView와 ViewPager2 예제를 추가해 학습한 내용을 코드로 구현하는 것이었습니다.

주요 변경사항
---
1) fragmentapp 모듈
- activity_main.xml: 상단 버튼 3개, fragment_container(FrameLayout), 하단 얇은 바(View id=bottom_bar, 4dp) 추가.
- MainActivity.kt: 버튼 클릭으로 BlankFragment / secondFragment / thirdFragment로 교체하고 하단 바 색상을 각 화면에 맞게 변경하도록 구현. savedInstanceState 체크로 초기화 중복 방지.
- 의존성: fragment-ktx 추가(androidx.fragment:fragment-ktx:1.8.9) — commit{} 확장 사용을 위해 필요.

2) secondFragment: RecyclerView 적용
- fragment_second.xml: RecyclerView(id=recycler_view) 추가.
- item_row.xml: 리스트 항목 레이아웃 추가.
- SecondAdapter.kt: 간단한 문자열 리스트 어댑터와 SpaceItemDecoration 구현.
- secondFragment.kt: LinearLayoutManager, DividerItemDecoration, SpaceItemDecoration 적용 및 샘플 데이터(20개)로 동작 확인.

3) viewpager 모듈 추가/수정
- activity_main.xml: ViewPager2(id=view_pager) 추가.
- PagerAdapter.kt: FragmentStateAdapter 구현 (3페이지).
- PageFragment.kt + fragment_page.xml: 각 페이지를 배경색과 텍스트로 표시.
- build.gradle.kts: viewpager2 및 fragment-ktx 의존성 추가.

빌드/검증
---
- :app:assembleDebug 성공 (fragmentapp 모듈)
- :app:viewpager:assembleDebug 성공 (viewpager 모듈)

실행 방법
---
Windows에서 루트 프로젝트에서 다음 명령을 사용:
- 전체 앱 빌드: .\\gradlew assembleDebug
- fragmentapp 모듈만: .\\gradlew :app:fragmentapp:assembleDebug
- viewpager 모듈만: .\\gradlew :app:viewpager:assembleDebug

다음 권장 작업
---
- 런타임에서 에뮬레이터/기기 실행 후 로그캣 확인
- ViewPager2에 페이저 인디케이터(Tab/점) 추가
- RecyclerView 아이템 클릭 핸들러 및 DiffUtil 적용

변경된 주요 파일
---
- app/fragmentapp/src/main/java/ai/hnu/kr/fragmentapp/MainActivity.kt
- app/fragmentapp/src/main/res/layout/activity_main.xml
- app/fragmentapp/src/main/res/layout/fragment_second.xml
- app/fragmentapp/src/main/res/layout/item_row.xml
- app/fragmentapp/src/main/java/ai/hnu/kr/fragmentapp/SecondAdapter.kt
- app/viewpager/src/main/java/ai/hnu/kr/viewpager/MainActivity.kt
- app/viewpager/src/main/java/ai/hnu/kr/viewpager/PagerAdapter.kt
- app/viewpager/src/main/java/ai/hnu/kr/viewpager/PageFragment.kt

문의
---
원하시면 ViewPager2 인디케이터, 수평/수직 전환, 또는 RecyclerView 개선(클릭/삭제 등)을 바로 적용해 드립니다.