# July 02 Application - Android Studio Projects

## 📅 학습 날짜: 2026-07-02

---

## 📚 학습 내용 요약

이 프로젝트는 Android Studio에서 다양한 레이아웃과 UI 컴포넌트를 학습하기 위한 모듈들로 구성되어 있습니다.

---

## 🏗️ 모듈별 상세 설명

### 1. **phonepad** 📱
**학습 주제**: 전화기 숫자 패드 UI 및 이벤트 처리

**주요 기능**:
- 전화기 표준 숫자 패드 레이아웃 (1-9, *, 0, #)
- 둥근 버튼 스타일 (`rounded_button.xml`)
- 숫자 입력 시 EditText에 표시
- 3가지 기능 버튼:
  - **⌫ (삭제)**: 마지막 숫자 제거
  - **📞 (다이얼링)**: 전화번호로 통화
  - **📹 (영상통화)**: 전화번호로 영상통화
- Toast 메시지를 통한 사용자 피드백

**학습 포인트**:
- TableLayout을 이용한 그리드 구성
- Button 이벤트 처리 (`setOnClickListener`)
- EditText 조작 (`append()`, `setText()`)
- Drawable을 이용한 커스텀 버튼 스타일

---

### 2. **relativelayout** 📐
**학습 주제**: RelativeLayout의 정렬 속성 및 위치 지정

**주요 학습 내용**:
- RelativeLayout을 사용한 자유로운 뷰 배치
- 뷰의 상대적 위치 지정:
  - `layout_centerInParent`: 중앙 배치
  - `layout_alignParentLeft/Right/Top/Bottom`: 부모 기준 정렬
  - `layout_alignAbove/toLeftOf`: 다른 뷰 기준 정렬
- 마진(margin)을 이용한 간격 조정
- 여러 TextView를 활용한 복합 레이아웃

**학습 포인트**:
- 상대적 레이아웃의 유연성
- 뷰 정렬의 다양한 옵션 이해
- 속성 조합을 통한 정교한 배치

---

### 3. **gravityexam** ⚖️
**학습 주제**: Gravity 속성을 이용한 콘텐츠 정렬

**주요 학습 내용**:
- LinearLayout에서 `gravity` 속성 사용
- `gravity="center|center_horizontal|center_vertical"`: 중앙 정렬
- 콘텐츠 내 텍스트 정렬 (`gravity="right"`)
- 뷰의 수평/수직 정렬 이해

**학습 포인트**:
- gravity와 layout_gravity의 차이 이해
- LinearLayout에서 정렬의 기본 개념
- TextView의 콘텐츠 정렬

---

### 4. **dogcatshow** 🐕🐈
**학습 주제**: 버튼 이벤트 처리 및 ImageView 표시/숨김 제어

**주요 기능**:
- 제목: "강아지, 고양이 보기"
- 2개의 버튼 (`layout_weight` 사용):
  - **강아지**: 강아지 이미지 표시
  - **고양이**: 고양이 이미지 표시
- ImageView의 가시성 제어 (`visibility` 속성):
  - `invisible`: 공간 유지하면서 숨김
  - `visible`: 표시
  - `gone`: 공간도 차지하지 않으면서 숨김

**학습 포인트**:
- LinearLayout의 `layout_weight` 사용법 (버튼 균등 분배)
- ImageView 활용
- visibility 속성의 3가지 상태 이해
- 버튼 클릭 이벤트 처리
- 여러 뷰의 상태 제어

---

## 🎯 종합 학습 목표

| 모듈 | 주요 개념 | 사용 컴포넌트 |
|------|---------|------------|
| phonepad | 이벤트 처리, 문자열 조작 | Button, EditText, TableLayout |
| relativelayout | 상대적 배치 | RelativeLayout, TextView |
| gravityexam | 정렬 속성 | LinearLayout, gravity |
| dogcatshow | 상태 제어, 가중치 | ImageView, Button, layout_weight |

---

## 💡 핵심 개념 정리

1. **레이아웃 종류**:
   - LinearLayout: 선형 배치 (수평/수직)
   - RelativeLayout: 상대적 배치
   - TableLayout: 표 형식 배치

2. **정렬 및 배치**:
   - gravity: 뷰 내 콘텐츠 정렬
   - layout_gravity: 부모 내 뷰의 위치
   - layout_weight: 공간 분배

3. **이벤트 처리**:
   - setOnClickListener: 클릭 이벤트
   - Toast: 임시 메시지 표시

4. **뷰 조작**:
   - setText/append: 텍스트 수정
   - visibility: 표시/숨김 제어
   - findViewById: 뷰 참조

---

## 📝 실행 방법

각 모듈은 독립적으로 실행 가능합니다:

```bash
# phonepad 앱 실행
# Build > Build Modules > phonepad

# relativelayout 앱 실행
# Build > Build Modules > relativelayout

# gravityexam 앱 실행
# Build > Build Modules > gravityexam

# dogcatshow 앱 실행
# Build > Build Modules > dogcatshow
```

---

## ✅ 완료 현황

- [x] phonepad: 전화기 숫자 패드 구현
- [x] relativelayout: RelativeLayout 학습
- [x] gravityexam: Gravity 속성 학습
- [x] dogcatshow: 버튼 이벤트 및 ImageView 제어

---

**작성일**: 2026-07-02 11:39
