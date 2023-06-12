INSERT INTO "user" (name, id, password, phone_number) VALUES ('조유정', 'admin', '$2a$10$XLXsZbafQ/6hGxZ6NQ3ZvOXbN/GkCBc1oP3/wJ/gC6qAay9tfe8b2', '010-1234-5678');
INSERT INTO "user" (name, id, password, phone_number) VALUES ('안수진', 'user', '$2a$10$XLXsZbafQ/6hGxZ6NQ3ZvOXbN/GkCBc1oP3/wJ/gC6qAay9tfe8b2', '010-1111-1111');


INSERT INTO setting (user_id, alram, detect_alram, sos_alram, sos_level) VALUES (1, 1, 1, 1, 1);

INSERT INTO report (type, content, phone_number, user_id, voice_id) VALUES ('REPORT_TYPE_FRAUD', '국민 은행에서 사기 전화','010-0000-0000', 1, 1);
INSERT INTO report (type, content, phone_number, user_id, voice_id) VALUES ('REPORT_TYPE_IMPERSONATING', '검찰청을 사칭해서 개인정보 빼돌리려는 전화였어요','010-0000-0001', 1, 2);

INSERT INTO report (type, content, phone_number, user_id, voice_id) VALUES ('REPORT_TYPE_FRAUD', '대구은행에서 걸려온 사기 전화','010-0000-0000', 2, 3);
INSERT INTO report (type, content, phone_number, user_id, voice_id) VALUES ('REPORT_TYPE_INDUCE', '국민 은행에서 사기 전화','010-0000-0001', 2, 4);

INSERT INTO sos (user_id, phone_number, relation, level) VALUES  (1, '01043213042', '가족', 3);
INSERT INTO sos (user_id, phone_number, relation, level) VALUES  (1, '01043213042', '친구', 2);
INSERT INTO sos (user_id, phone_number, relation, level) VALUES  (1, '01043213042', '선임', 1);


INSERT INTO voice (text) VALUES ('여보세요 안녕하세요 고객님 김종현 대리입니다 아네 대리님 고객님 연락이 너무 안되셔가지고 지금 아 네 제가 너무 바빠서요 아 지금은 좀 연락이 이제 가능하신 건가요 아네네 대리님 그 남의 돈 드시니까 좋으세요 네 무슨말씀이시죠 제가 보니까 제게 상환이 다 안되있더라구요 상환이 안되셨다구요 네 어디가 상환이 안되셨다는 거죠 아 제거 KB국민은행에 상환이 다 안되있던데요 고객센터에서 알아보신 건가요 네 어디에 상환하신거 수요일에 상환하신 아닌데 잠시만요 목요일날 상환하신거 말씀하신거죠 네 그게 지금 고객센터로 넘어가는게 좀 서류가 늦게 들어가서 확인이 안되실수도 있으세요 어떻게 기록삭제가 바로되는데 확인이 안돼요 그쪽에서 기록삭제 하고 납부하셨잖아요');