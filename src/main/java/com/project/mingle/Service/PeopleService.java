package com.project.mingle.Service;

import com.project.mingle.Dto.PeopleDto;
import com.project.mingle.Entity.People;
import com.project.mingle.Repository.PeopleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PeopleService {
    private final PeopleRepository peopleRepository;

    public People savePeople(PeopleDto peopledto){
        People people = new People();
        people.setName(peopledto.getName());

        return peopleRepository.save(people);
    }
}
