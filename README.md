spring.application.name=twinstar

# 포트번로 설정
server.port=9998
server.servlet.context-path=/twinstar

# 파일이름 한긍 인코딩
spring.mandatory-file-encoding=UTF-8

# 서버에 업로드 파일용량 설정. 50,000,000
spring.servlet.multipart.max-file-size=150MB
spring.servlet.multipart.max-request-size=150MB

# application.properties
## 본인제미나이 api 키 입력 !!!!!!!!!!!!!

#인텔리제이 옵션
spring.output.ansi.enabled= always

#jpa 설정
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://127.0.0.1/jpadb
spring.datasource.username=root
spring.datasource.password= ## 본인 db 비밀번호 입력
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.format_sql=true
