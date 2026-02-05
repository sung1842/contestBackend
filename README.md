# 기능 및 구현 명세서
## springboot 프로젝트
1. spingboot, mysql, jwt 로그인 인증 라이브러리 사용
## gemini api를 이용한 서비스 구축
1. ai 서버에서 받아온 데이터를 토대로 연예인 최근 1년 패션 트렌드 기능 및 구현 명세서
springboot 프로젝트
1. spingboot, mysql, jwt 로그인 인증 라이브러리 사용
## gemini api를 이용한 서비스 구축
1. ai 서버에서 받아온 데이터를 토대로 연예인 최근 1년 패션 트렌드 분석. (선호하는 색상, 스타일, 아이템)
2. 세부적인 옷 데이터를 분석해서 네이버 쇼핑으로 연결

spring.application.name=twinstar

### 포트번로 설정
server.port=9998
server.servlet.context-path=/twinstar

### 파일이름 한긍 인코딩
spring.mandatory-file-encoding=UTF-8

### 서버에 업로드 파일용량 설정. 50,000,000
spring.servlet.multipart.max-file-size=150MB
spring.servlet.multipart.max-request-size=150MB

### application.properties
#### 본인제미나이 api 키 입력 !!!!!!!!!!!!!

### 인텔리제이 옵션
spring.output.ansi.enabled= always

#### jpa 설정
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://127.0.0.1/jpadb
spring.datasource.username=root
spring.datasource.password= ## 본인 db 비밀번호 입력
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.format_sql=true
