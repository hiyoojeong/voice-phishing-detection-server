package PhishingUniv.Phinocchio.domain.Doubt.service;

import PhishingUniv.Phinocchio.domain.Doubt.dto.DoubtRequestDto;
import PhishingUniv.Phinocchio.domain.Doubt.dto.MLRequestDto;
import PhishingUniv.Phinocchio.domain.Doubt.dto.MLResponseDto;
import PhishingUniv.Phinocchio.domain.Doubt.entity.DoubtEntity;
import PhishingUniv.Phinocchio.domain.Doubt.repository.DoubtRepository;
import PhishingUniv.Phinocchio.domain.FCM.service.FCMNotificationService;
import PhishingUniv.Phinocchio.domain.Login.repository.UserRepository;
import PhishingUniv.Phinocchio.domain.Report.entity.ReportEntity;
import PhishingUniv.Phinocchio.domain.Setting.entity.SettingEntity;
import PhishingUniv.Phinocchio.domain.Setting.repository.SettingRepository;
import PhishingUniv.Phinocchio.domain.Sos.dto.MessageDTO;
import PhishingUniv.Phinocchio.domain.Sos.dto.SmsResponseDTO;
import PhishingUniv.Phinocchio.domain.Sos.entity.SosEntity;
import PhishingUniv.Phinocchio.domain.Sos.service.SmsService;
import PhishingUniv.Phinocchio.domain.Sos.service.SosService;
import PhishingUniv.Phinocchio.domain.User.entity.UserEntity;
import PhishingUniv.Phinocchio.domain.Voice.entity.VoiceEntity;
import PhishingUniv.Phinocchio.domain.Voice.repository.VoiceRepository;
import PhishingUniv.Phinocchio.exception.Doubt.DoubtAppException;
import PhishingUniv.Phinocchio.exception.Doubt.DoubtErrorCode;
import PhishingUniv.Phinocchio.exception.FCM.FCMAppException;
import PhishingUniv.Phinocchio.exception.FCM.FCMErrorCode;
import PhishingUniv.Phinocchio.exception.Login.InvalidJwtException;
import PhishingUniv.Phinocchio.exception.Login.LoginAppException;
import PhishingUniv.Phinocchio.exception.Login.LoginErrorCode;
import PhishingUniv.Phinocchio.exception.Setting.SettingAppException;
import PhishingUniv.Phinocchio.exception.Setting.SettingErrorCode;
import PhishingUniv.Phinocchio.exception.Sos.SosAppException;
import PhishingUniv.Phinocchio.exception.Sos.SosErrorCode;
import PhishingUniv.Phinocchio.exception.Voice.VoiceAppException;
import PhishingUniv.Phinocchio.exception.Voice.VoiceErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.twilio.twiml.voice.Say.Voice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@RestController
public class DoubtService {

    private final MLService mlService;

    private final DoubtRepository doubtRepository;

    private final SettingRepository settingRepository;

    private final VoiceRepository voiceRepository;

    private final UserRepository userRepository;

    private final SosService sosService;

    private final SmsService smsService;

    private final FCMNotificationService fcmNotificationService;

    public String getCurrentTime() {
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a hh:mm");

        StringBuilder sb = new StringBuilder();
        sb.append(nowDate.getYear())
                .append('-')
                .append(nowDate.getMonthValue())
                .append('-')
                .append(nowDate.getDayOfMonth())
                .append(' ')
                .append(nowTime.format(formatter))
                .append(" 통화내역");

        return sb.toString();
    }

    //의심 목록 조회
    public List<DoubtEntity> getDoubtList() throws InvalidJwtException{
        String ID = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepository.findById(ID).orElseThrow(
                ()->new InvalidJwtException(LoginErrorCode.JWT_USER_NOT_FOUND));

        Long userId = userEntity.getUserId();
        List<DoubtEntity> doubtEntities = doubtRepository.findDoubtEntitiesByUserId(userId);

        return doubtEntities;

    }
    private void addDoubt(DoubtRequestDto doubtRequestDto, String text, int level) throws InvalidJwtException, DoubtAppException, VoiceAppException {
        // userId 불러오기
        String ID = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepository.findById(ID).orElseThrow(
                ()->new InvalidJwtException(LoginErrorCode.JWT_USER_NOT_FOUND));

        Long userId = userEntity.getUserId();

        // 목소리 저장
        VoiceEntity voiceEntity = new VoiceEntity();
        voiceEntity.setText(text);
        VoiceEntity savedVoiceEntity = voiceRepository.save(voiceEntity);
        if(savedVoiceEntity == null) {
            throw new DoubtAppException(DoubtErrorCode.FAILED_TO_SAVE);
        }

        // 의심내역 저장
        DoubtEntity doubtEntity = new DoubtEntity();
        doubtEntity.setPhoneNumber(doubtRequestDto.getPhoneNumber());
        doubtEntity.setLevel(level);
        doubtEntity.setUserId(userId);
        doubtEntity.setTitle(getCurrentTime());
        doubtEntity.setVoice_id(savedVoiceEntity.getVoiceId());
        DoubtEntity savedDoubtEntity = doubtRepository.save(doubtEntity);
        System.out.println(savedDoubtEntity);
        if(savedDoubtEntity == null) {
            throw new VoiceAppException(VoiceErrorCode.FAILED_TO_SAVE);
        }

    }

    private void sendSms(int level) throws UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException, InvalidKeyException,
                                                        JsonProcessingException, LoginAppException, SosAppException {

        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        // 메세지 설정
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new LoginAppException(LoginErrorCode.USERNAME_NOT_FOUND));
        String userPhone = userEntity.getPhoneNumber();
        String smsMsg = "[피노키오] " + userPhone + " 번호로 보이스피싱이 감지되었습니다. <" + level +"단계>";

        MessageDTO messageDTO = MessageDTO.builder()
                .content(smsMsg).build();

        // 메세지 보내기
        List<SosEntity> sosEntities = sosService.getSosListByLevel(level);
        for(SosEntity sosEntity : sosEntities) {
            messageDTO.setTo(sosEntity.getPhoneNumber());
            SmsResponseDTO smsResponseDTO = smsService.sendSms(messageDTO);
            if(!smsResponseDTO.getStatusCode().equals("202"))
                throw new SosAppException(SosErrorCode.FAILED_TO_SEND_SMS);
        }
    }

    public ResponseEntity<?> doubt(DoubtRequestDto doubtRequestDto) throws UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException, InvalidKeyException,
            JsonProcessingException, InvalidJwtException, DoubtAppException, SettingAppException {

        // userId 불러오기
        String ID = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepository.findById(ID).orElseThrow(
                ()->new InvalidJwtException(LoginErrorCode.JWT_USER_NOT_FOUND));

        Long userId = userEntity.getUserId();

        // 머신러닝 서버로 mlRequestDto를 전송하고 응답을 받음
        MLRequestDto mlRequestDto = new MLRequestDto(doubtRequestDto.getText());
        MLResponseDto mlResponseDto = mlService.processText(mlRequestDto);
        if(mlRequestDto == null) {
            throw new DoubtAppException(DoubtErrorCode.DISCONNCECTED_TO_MLSERVER);
        }


        // 보이스피싱 의심내역 저장
        String text = doubtRequestDto.getText();
        int level = mlResponseDto.getLevel();
        if(level > 0) {
            addDoubt(doubtRequestDto, text, level);
        }

//        // 알람 설정 확인
//        SettingEntity settingEntity = settingRepository.findByUserId(userId)
//                .orElseThrow(() -> new SettingAppException(SettingErrorCode.SETTING_NOT_FOUND));
//
//        // 보이스피싱 알람 설정 안 한 경우
//        if(!settingEntity.getDetectAlram())
//            return ResponseEntity.ok("not set detect alram");
//
//        // 긴급 연락처 알람 설정 한 경우
//        if(settingEntity.getSosAlram() && level > 0) {
//            sendSms(level);
//        }

        if(level > 0) {
            // 긴급 연락처 알람 전송
            sendSms(level);

            // 보이스피싱 알람 설정 한 경우: push 알람 보내기
            String fcmToken = getFcmToken(ID);
            String fcmMessageTitle = setFcmMessageTitle();
            String fcmMessageBody = setFcmMessageBody(level);
            sendFCMNotification(fcmToken, fcmMessageTitle, fcmMessageBody);
        }
        return ResponseEntity.ok(mlResponseDto);
    }

    private String getFcmToken(String id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new LoginAppException(LoginErrorCode.USERNAME_NOT_FOUND));
        return user.getFcmToken();
    }

    private void sendFCMNotification(String fcmToken, String title, String body) {
        try {
            fcmNotificationService.sendPushNotification(fcmToken,title, body);
        } catch (Exception e) {
            throw new FCMAppException(FCMErrorCode.FCM_ERROR);
        }
    }

    private String setFcmMessageTitle() {
        return "[피노키오] 보이스피싱으로 의심되는 통화가 발견되었습니다.";
    }

    private String setFcmMessageBody(int level) {
        StringBuilder fcmMessage = new StringBuilder();
        fcmMessage.append("보이스피싱 \"");
        if(level == 1) {
            fcmMessage.append("의심");
        } else if(level == 2) {
            fcmMessage.append("경고");
        } else if(level == 3) {
            fcmMessage.append("위험");
        }
        fcmMessage.append("\" 단계입니다.");

        return fcmMessage.toString();
    }

}
