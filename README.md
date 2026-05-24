# 수강 신청 시스템

## 1. 프로젝트 개요

* 크리에이터(강사)는 강의를 개설하고 수강 정원, 가격, 기간을 설정합니다.
* 클래스메이트(수강생)는 원하는 강의에 수강 신청을 합니다.
* 정원이 초과되면 신청이 불가합니다.
* 신청 후 결제가 완료되어야 수강 확정됩니다.



## 2. 기술 스택

### Backend

<p>
  <img src="https://img.shields.io/badge/Java 17-007396?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/Spring Boot 3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
  <img src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white">
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
</p>

### Test

<p>
  <img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white">
  <img src="https://img.shields.io/badge/Mockito-78A641?style=for-the-badge&logo=mockito&logoColor=white">
  <img src="https://img.shields.io/badge/AssertJ-EA4AAA?style=for-the-badge&logoColor=white">
</p>



## 3. 실행 방법

### application.yml 설정

```yaml
spring:
  application:
    name: class-enrollment-system

  datasource:
    url: jdbc:mysql://localhost:3307/class_enrollment
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
        show-sql: true
```

→ 환경에 맞게 username과 password를 입력합니다.



## 4. 요구사항 해석 및 가정

### 1. 사용자 구분

- userId를 X-USER-ID 헤더로 전달하는 방식을 사용해 사용자 식별을 간략히 처리했습니다.

```text
X-USER-ID: 1
```

### 2. 강의 상태

```text
DRAFT → OPEN → CLOSED

DRAFT: 초안 (신청 불가)
OPEN: 모집 중 (신청 가능)
CLOSED: 모집 마감 (신청 불가)
```

- 강의 등록 시 초기 상태는 DRAFT입니다.
- 강사는 자신이 개설한 강의의 상태를 변경할 수 있습니다.
- 정원에 도달하면 CLOSED 상태로 자동 변경됩니다.
- 수강 취소로 인해 자리가 생기면 OPEN 상태로 다시 변경됩니다.

### 3. 수강 신청 상태

```text
PENDING → CONFIRMED → CANCELLED

PENDING: 신청 완료, 결제 대기
CONFIRMED: 결제 완료, 수강 확정
CANCELLED: 취소됨
```

- 결제 확정 처리는 자신이 신청한 강의에 대해 단순 상태 변경 방식으로 대체했습니다.
- CONFIRMED 상태의 신청만 CANCELLED 상태로 변경할 수 있습니다.

### 4. 정원 반영 기준

- PENDING(신청 완료, 결제 대기) 상태는 신청 인원에 반영되지 않습니다.
- CONFIRMED(결제 완료, 수강 확정) 상태에서만 신청 인원이 증가하도록 구현했습니다.
- CANCELLED(취소됨) 상태로 변경되는 경우에는 신청 인원이 감소하도록 구현했습니다.



## 5. 설계 결정과 이유

### 1. DTO 분리

필요한 데이터만 반환하도록 DTO를 기능별로 분리했습니다.

- CourseListResponse
- CourseDetailResponse
- CreateEnrollmentResponse

### 2. 정적 팩토리 메서드 사용

```text
User.create(...)
Course.create(...)
Enrollment.create(...)
```

### 3. 동시성 제어 적용

동시에 여러 사람이 마지막 자리를 결제 완료하는 경우와 수강 취소로 인해 정원이 변경되는 상황을 고려하여 비관적 락(Pessimistic Lock)을 적용했습니다.

```text
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

### 4. 예외 처리 적용

- `@RestControllerAdvice` 기반 글로벌 예외 처리를 적용했습니다.
- 예외 발생 시 일관된 형식의 응답을 반환하도록 구성했습니다.

```json
{
  "status": 400,
  "message": "이미 신청한 강의입니다."
}
```



## 6. 미구현 / 제약사항

### 미구현 기능

- [ ] 수강 취소 시 취소 가능 기간 제한 (예: 결제 후 7일 이내)
- [ ] 대기열(waitlist) 기능
- [ ] 강의별 수강생 목록 조회 (크리에이터 전용)
- [ ] 신청 내역 페이지네이션

### 제약사항

- JWT 기반 인증/인가
- 외부 결제 시스템 연동



## 7. AI 활용 범위

- 동시성 제어 방식 검토
- 예외 처리 구조 개선
- 테스트 코드 작성 학습



## 8. API 목록 및 예시

### 1. 강의 등록

#### Request

```text
POST /api/v1/courses
```

#### Header

```text
X-USER-ID: 1
```

#### Body

```json
{
  "title": "Spring Boot 심화",
  "description": "팀 프로젝트 진행 예정",
  "price": 150000,
  "capacity": 30,
  "startDate": "2026-10-01",
  "endDate": "2026-12-23"
}
```

### 2. 강의 목록 조회

#### Request

```text
GET /api/v1/courses
```

### 3. 상태 필터를 포함한 강의 목록 조회

#### Request

```text
GET /api/v1/courses?status=OPEN
```

### 4. 강의 상세 조회

#### Request

```text
GET /api/v1/courses/{courseId}
```

### 5. 강의 상태 변경

#### Request

```text
PATCH /api/v1/courses/{courseId}/status
```

#### Body

```json
{
  "status": "OPEN"
}
```

### 6. 수강 신청

#### Request

```text
POST /api/v1/courses/{courseId}/enrollments
```

#### Header

```text
X-USER-ID: 2
```

### 7. 결제 확정 처리

#### Request

```text
PATCH /api/v1/enrollments/{enrollmentId}/confirm
```

### 8. 수강 취소

#### Request

```text
PATCH /api/v1/enrollments/{enrollmentId}/cancel
```

### 9. 내 수강 신청 목록 조회

#### Request

```text
GET /api/v1/enrollments/my
```



## 9. 데이터 모델 설명

<img width="1080" height="766" alt="Image" src="https://github.com/user-attachments/assets/63d82564-138a-4919-ba93-9d3f862f9717" />



## 10. 테스트 실행 방법

### 테스트 항목

#### 1. CourseServiceTest

- 강의 등록
- 강의 상태 변경

#### 2. EnrollmentServiceTest

- 수강 신청
- 수강 확정
- 수강 취소
