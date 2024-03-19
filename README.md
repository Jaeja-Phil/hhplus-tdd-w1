# 항해 1주차 과제 - TDD

## 과제 링크

> [노션](https://www.notion.so/teamsparta/Chapter-1-1-Test-Driven-Development-ea474c19ad7b476495b7f3f9abcc690f)

## 요구 사항

### [과제] `point` 패키지의 TODO 와 테스트코드를 작성해주세요.

- PATCH  `/point/{id}/charge` : 포인트를 충전한다.
- PATCH `/point/{id}/use` : 포인트를 사용한다.
- GET `/point/{id}` : 포인트를 조회한다.
- GET `/point/{id}/histories` : 포인트 내역을 조회한다.
- 잔고가 부족할 경우, 포인트 사용은 실패하여야 합니다.
- 동시에 여러 건의 포인트 충전, 이용 요청이 들어올 경우 순차적으로 처리되어야 합니다.

## 제한 사항

- Nest.js 의 경우 Typescript , Spring 의 경우 Kotlin / Java 중 하나로 작성합니다.
  - 프로젝트에 첨부된 설정 파일은 수정하지 않도록 합니다.
- 테스트 케이스의 작성 및 작성 이유를 주석으로 작성하도록 합니다.
- 프로젝트 내의 주석을 참고하여 필요한 기능을 작성해주세요.
- 분산 환경은 고려하지 않습니다.

---

# 풀이 Pseudocode

### 1. 고려사항

동시성 제어가 필요하며, 순차적으로 처리되어야 함.
- gradle 파일을 건드릴 수 없으므로 redis와 같은 큐를 사용할 수 없음
- 이를 해결하기 위해 중간 레이어 (service)를 통해 순차적으로 처리하도록 함

TDD에 맞게 테스트 코드를 먼저 작성하고, 그에 맞게 코드를 작성 시도.

### 2. 동시성 제어

각 테이블(PointHistory, UserPoint)별로 각각 동시성 제어가 필요함
- service 레이어를 만들어 crud를 처리하도록 함
- 실제 동시성 처리는 helper 같은 담당자를 만들어 처리하게 함  
