package com.project.mingle.Controller;

import com.project.mingle.Entity.People;
import com.project.mingle.Dto.PeopleDto;
import com.project.mingle.Service.PeopleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PeopleController {
    private final PeopleService peopleService;

    // @RequestBody는 Json을 input으로 준다.
    // @RequestParam은
    @PostMapping("/People")
    public People postPeople(@RequestBody PeopleDto peopledto){
        return peopleService.savePeople(peopledto);
    }
}
