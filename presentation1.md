---
marp: true
theme: gaia
class: lead
paginate: true
backgroundColor: #0c111a
color: #f0f4f8
style: |
  section {
    font-family: 'Pretendard', 'Malgun Gothic', sans-serif;
    padding: 40px;
  }
  h1 {
    color: #1ec800;
  }
  h2 {
    color: #1ec800;
    border-bottom: 2px solid #1ec800;
  }
  footer {
    color: #657786;
  }
  .highlight {
    background-color: rgba(30, 200, 0, 0.1);
    padding: 10px;
    border-left: 4px solid #1ec800;
    border-radius: 4px;
  }
  .grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }
---

# :date: Naver Calendar Plus
### UI/UX 개선 및 제스처 중심 기능 고도화

모바일 프로그래밍 기획 발표

**발표자:** 김태현

---

## 기존 서비스의 페인 포인트

### 1. 입력 뎁스의 번거로움
- 간단한 루틴이나 메모성 일정을 추가할 때도 매번 **화면 전체가 전환**됨
- 제목, 시간, 알림 등을 긴 단계를 거쳐 일일이 세팅해야 하는 불필요한 공수 발생

### 2. 데이터 시각화의 한계
- 달력 화면이 **단순 텍스트 나열 위주**로 구성되어 있음
- 이번 달에 학업, 여가, 운동 등에 시간을 어떻게 배분했는지 직관적 파악 불가

---

## :zap: 개선 1: 길게 눌러 바로 등록하기 (Quick Add)

<div class="grid">
<div>

### 롱 클릭(Long Click) 제스처
- 화면 전체를 이동시킬 필요 없이, 원하는 특정 날짜 셀을 **길게 누르는 제스처**를 인식해 작동합니다.

### 간편 팝업 레이아웃
- 제목과 카테고리 태그 선택만으로 끝내는 간편 인풋 다이얼로그 팝업을 띄워 터치 동선을 획기적으로 줄입니다.
</div>
<div class="highlight">

### :bulb: 기대 효과
사용자가 매주 반복 입력하는 간단한 약속이나 루틴 일정을 **단 두 번의 터치**만으로 완성하도록 설계합니다.
</div>
</div>

---

## :art: 개선 2: 컬러 태깅 및 D-Day 하이라이트

- **직관적인 뷰 구성 (Color Dot UI)**
  - 공부, 운동, 휴식 등 사용자가 선택한 카테고리 컬러 고유값이 월간 캘린더 날짜 하단에 미니 컬러 점들로 실시간 표현됩니다.

- **중요 마감일(D-Day) 지정 옵션**
  - 중요 옵션을 체크하면, 하단 목록 뷰 최상단에 **마감 임박 잔여 시간**과 함께 하이라이트 노출되어 과제 및 시험 마감을 놓치지 않게 합니다.

---

## 프로젝트 개발 로드맵 (Roadmap)

- **Phase 1:** 안드로이드 프로젝트 환경 구축 및 메인 달력, 팝업 UI 설계
- **Phase 2:** 로컬 DB(`Room`) 설계 및 연동 (카테고리 코드 & D-Day 필드 구축)
- **Phase 3:** 롱 클릭 제스처 동작 구현 및 D-Day 최상단 우선순위 정렬 로직 고도화
- **Phase 4:** 날짜 셀 미니 컬러 도트 매핑 완성 및 최종 예외 처리 테스트

---

<!-- class: lead -->

# Thanks
### 감사합니다.

모바일 프로그래밍 과제 발표 | 발표자: 김태현