package com.example.ZverevaDanceWCS.service.model.user.studentInfo;

import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InfoService {
    @Autowired
    public final InfoRepository infoRepository;

    public InfoService(InfoRepository infoRepository) {
        this.infoRepository = infoRepository;
    }

    public StudentInfo save(StudentInfo info) {
        return infoRepository.save(info);
    }

    public StudentInfo saveFromNewInfoDTO(NewInfoDTO infoDto, Long trainerId) {
        StudentInfo info = new StudentInfo();
        info.setTrainerId(trainerId);
        info.setStudentId(infoDto.studentId);
        info.setInfo(infoDto.info);
        info.setStatus(InfoStatus.ACTUAL);
        info.setDate(LocalDate.now());
        return infoRepository.save(info);
    }

    public StudentInfo findById(int infoId) {
        if (infoRepository.findById(infoId).isPresent()) {
            return infoRepository.findById(infoId).get();
        } else {
            throw new NotFoundException("student info with id " + infoId + " not found.");
        }
    }


    public String findByStudentActual(Long studentId) {
        List<StudentInfo> info = infoRepository.findByStudentIdAndStatus(studentId, InfoStatus.ACTUAL);
        info.sort(StudentInfo.byDate());
        return info.stream().map(StudentInfo::toString).collect(Collectors.joining("\n"));
    }

}
