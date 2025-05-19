# Challenge.with

> **프론트엔드 GitHub** : https://github.com/YunJaeHoon/Challenge.with-front
>
>
> **백엔드 GitHub** : https://github.com/YunJaeHoon/Challenge.with-back
>

## 개요

---

- 개인으로 진행 중인 챌린지 참여 플랫폼 개발 프로젝트입니다.
- 기획 & 디자인 & 프론트엔드 개발 & 백엔드 개발 등 모든 작업을 혼자서 수행 중입니다.
- 혼자서 또는 친구들과 함께 챌린지를 진행하고 참여 정보를 확인할 수 있는 서비스입니다.
- 현재 진행 중인 프로젝트입니다.

## 주요 기능

---

- 회원가입 및 로그인
- 사용자 간의 친구 요청/삭제/차단
- 알림, 문의, 공지사항
- 일/주/월 단위의 챌린지를 생성 및  다른 사용자가 생성한 챌린지 가입
- 본인의 페이즈 참여 현황 변경
- 챌린지 참가자들의 참여 통계 정보 조회
- 본인과 다른 사람들의 챌린지 참여 정보 조회
- 프리미엄 권한에 따라, 증거사진 등록 등의 프리미엄 서비스 제공

## 핵심 로직

---

- Spring Security를 활용한 인증 처리 ( 일반 로그인 및 OAuth 2.0 )
- AWS S3를 활용한 증거사진 등록 및 삭제
- AOP를 활용한 프리미엄 권한 확인 로직
- JavaMailSender를 활용한 인증번호 이메일 전송
- @Async, ThreadPoolTaskExecutor를 활용한 이메일 전송 요청 비동기 처리
- Redis를 활용한 인증번호 및 인증번호 확인 정보 관리
- RabbitMQ를 활용한 페이즈 참여 정보 갱신 요청 비동기 처리
- Spring Batch 및 Spring Scheduler를 활용한 데이터 일괄 처리 스케줄링
- 성능을 위한 의도적인 데이터 비정규화
- 확장성 및 유지보수성을 고려한 디자인 패턴 적용
- 인덱스를 활용한 빠른 데이터베이스 조회
- Unique constraint를 통한 데이터 무결성 보장

## 와이어프레임

https://www.figma.com/board/oMGyaiPFxrKeX1qyd8EZKp/Challenge.with-%EC%99%80%EC%9D%B4%EC%96%B4%ED%94%84%EB%A0%88%EC%9E%84?node-id=0-1&p=f&t=Tf1B83Nn3CSDM3BQ-0
