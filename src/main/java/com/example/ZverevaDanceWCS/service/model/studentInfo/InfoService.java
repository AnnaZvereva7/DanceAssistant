package com.example.ZverevaDanceWCS.service.model.studentInfo;

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

    public StudentInfo saveFromNewInfoDTO(NewInfoDTO infoDto) {
        StudentInfo info = new StudentInfo();
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

    public List<StudentInfo> findAllByStudentId(int studentId) {
        List<StudentInfo> info = infoRepository.findByStudentId(studentId);
        info.sort(StudentInfo.byDate());
        return info;
    }

    public String findByStudentActual(int studentId) {
        List<StudentInfo> info = infoRepository.findByStudentIdAndStatus(studentId, InfoStatus.ACTUAL);
        info.sort(StudentInfo.byDate());
        return info.stream().map(StudentInfo::toString).collect(Collectors.joining("\n"));
    }

    public HashMap<Integer, String> findAllByStatus(InfoStatus status) {
        List<StudentInfo> info = infoRepository.findByStatus(status);
        HashMap<Integer, List<StudentInfo>> infoMap = new HashMap<>();
        for (StudentInfo i : info) {
            List<StudentInfo> infoList = infoMap.get(i.studentId);
            if (infoList != null) {
                infoList.add(i);
                infoMap.put(i.studentId, infoList);
            } else {
                List<StudentInfo> newList = new ArrayList<>();
                newList.add(i);
                infoMap.put(i.studentId, newList);
            }
        }
        HashMap<Integer, String> responce = new HashMap<>();
        for (Map.Entry<Integer, List<StudentInfo>> entry : infoMap.entrySet()) {
            List<StudentInfo> studentInfoList = entry.getValue();
            studentInfoList.sort(StudentInfo.byDate());
            String infoString = studentInfoList.stream().map(StudentInfo::toString).collect(Collectors.joining("\n"));
            responce.put(entry.getKey(), infoString);
        }
        return responce;
    }


}
