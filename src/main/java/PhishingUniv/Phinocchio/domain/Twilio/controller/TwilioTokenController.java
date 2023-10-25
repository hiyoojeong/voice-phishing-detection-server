package PhishingUniv.Phinocchio.domain.Twilio.controller;

import PhishingUniv.Phinocchio.domain.Twilio.dto.TwilioTokenDto;
import PhishingUniv.Phinocchio.domain.Twilio.dto.TwilioTokenRequestDto;
import PhishingUniv.Phinocchio.domain.Twilio.service.TwilioTokenService;
import com.twilio.jwt.accesstoken.AccessToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/twilio")
@RestController
@RequiredArgsConstructor
public class TwilioTokenController {

    private final TwilioTokenService twilioTokenService;


    @PostMapping("/token")
    public ResponseEntity<?> generateToken(@RequestBody TwilioTokenRequestDto twilioTokenRequestDto) {
        AccessToken token = twilioTokenService.generateToken(twilioTokenRequestDto);

        return ResponseEntity.ok().body(new TwilioTokenDto(token.toJwt()));
    }
}

