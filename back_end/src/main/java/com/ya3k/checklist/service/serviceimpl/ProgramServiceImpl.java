package com.ya3k.checklist.service.serviceimpl;

import com.ya3k.checklist.enumm.StatusEnum;
import com.ya3k.checklist.dto.ProgramDto;
import com.ya3k.checklist.entity.Program;
import com.ya3k.checklist.entity.Tasks;
import com.ya3k.checklist.entity.Users;
import com.ya3k.checklist.event.eventhandle.ProgramEventHandle;
import com.ya3k.checklist.mapper.ProgramMapper;
import com.ya3k.checklist.repository.ProgramRepository;
import com.ya3k.checklist.repository.TasksRepository;
import com.ya3k.checklist.repository.UserRepository;
import com.ya3k.checklist.dto.response.programresponse.ProgramResponse;
import com.ya3k.checklist.service.serviceinterface.ProgramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ProgramServiceImpl implements ProgramService {


    private final ProgramRepository programRepository;
    private final TasksRepository tasksRepository;
    private final UserRepository urepo;
    private final ApplicationEventPublisher eventPublisher;


    @Autowired
    public ProgramServiceImpl(ProgramRepository programRepository, TasksRepository tasksRepository, UserRepository urepo, ApplicationEventPublisher eventPublisher) {
        this.programRepository = programRepository;
        this.tasksRepository = tasksRepository;
        this.urepo = urepo;
        this.eventPublisher = eventPublisher;
    }


    @Override
    public ProgramDto createProgram(ProgramDto programDto, String userName) {
        Program program = ProgramMapper.mapDtoToProgram(programDto);
        Users user = urepo.findByUser(userName);
        program.setUser(user);
        if (programDto.getName() != null) {
            String trimmedName = programDto.getName().trim();
            program.setName(trimmedName);
        }

        if (program.getStatus() == null || program.getStatus().isEmpty()) {
            program.setStatus(StatusEnum.IN_PROGRESS.getStatus());
        }

        //set create time
        program.setCreateTime(LocalDateTime.now());

        if (programDto.getEndTime() != null) {
            program.setEndTime(programDto.getEndTime());
        } else {
            throw new IllegalArgumentException("End time is required.");
        }

        Program savedProgram = programRepository.save(program);
        return ProgramMapper.mapToDto(savedProgram);
    }

    @Override
    public Page<ProgramResponse> findByUserAndFilters(String username, String status, LocalDate endTime, String programName, Pageable pageable) {
        Page<Program> programs = programRepository.findByUserAndFilters(username, status, endTime, programName, pageable);

        return programs.map(ProgramResponse::fromProgram);
    }

    @Override
    public ProgramDto deleteById(int id) {

        Program program = programRepository.deleteById(id);
        if (program != null) {
            return ProgramMapper.mapToDto(program);
        }
        return null;
    }

    @Override
    public ProgramDto findByProgramId(int id) {
        Program program = programRepository.findByProgramId(id);
        if (program != null) {
            return ProgramMapper.mapToDto(program);
        }
        return null;
    }


    //auto update status of program base on task status
    @Override
    public void autoUpdateStatusByTaskStatus(int taskId) {
        Tasks tasks = tasksRepository.findByTasksId(taskId);
        //handler auto update status for program
        Program program = tasks.getProgram();
        List<Tasks> allTasks = program.getListTask();
        log.debug("Program {} has {} tasks", program.getName(), allTasks.size());

        log.debug("All tasks are completed");
        boolean allTasksCompleted = allTasks.stream().allMatch(t -> StatusEnum.COMPLETED.getStatus().equals(t.getStatus()));

        log.debug("Some tasks are in progress");
        boolean tasksIsInProgress = allTasks.stream().anyMatch(t -> StatusEnum.IN_PROGRESS.getStatus().equals(t.getStatus()));

        log.debug("Some tasks are missed deadline");
        boolean tasksIsMissDeadline = allTasks.stream().anyMatch(t -> StatusEnum.MISS_DEADLINE.getStatus().equals(t.getStatus()));
        // Update program status if all tasks are completed
        if (allTasksCompleted) {
            log.debug("Program {} is completed", program.getName());
            program.setStatus(StatusEnum.COMPLETED.getStatus());
        }
        if (tasksIsInProgress && !tasksIsMissDeadline) {
            log.debug("Program {} is in progress", program.getName());
            program.setStatus(StatusEnum.IN_PROGRESS.getStatus());
        }


        programRepository.save(program);
        // Publish program status change event
        eventPublisher.publishEvent(new ProgramEventHandle(this, program));
    }


    /*
    0: The second when the task should run (0 seconds).
    1: The minute when the task should run (1 minute).
    0: The hour when the task should run (0 hours, which corresponds to 24:00 or 12:00 AM).
    *: The day of the month (any day).
    *: The month (any month).
    *: The day of the week (any day of the week).
    */
    //update program status base on deadline
    //    @Scheduled(fixedRate = 60000)
//    @Scheduled(cron = "0 1 0 * * *") // Run at 12:01 AM every day
    @Scheduled(cron = "0 * * * * *")

    @Override
    public void updateProgramStatusBaseOnDeadline() {
        LocalDate currentDate = LocalDate.now();
        List<Tasks> tasksList = tasksRepository.findByEndTimeGreaterThan(currentDate);

        //if no task found
        if (tasksList.isEmpty()) {
            return;
        }

        //update status for tasks
        for (Tasks task : tasksList) {
            log.debug("Task {} is missed deadline", task.getTaskName());
            task.setStatus(StatusEnum.MISS_DEADLINE.getStatus());
            tasksRepository.save(task);
        }

        //update status for program
        for (Tasks task : tasksList) {
            Program program = programRepository.findById(task.getProgram().getId()).orElse(null);
            if (program != null && program.getStatus().equals(StatusEnum.IN_PROGRESS.getStatus())) {
                log.debug("Program {} is missed deadline", program.getName());
                program.setStatus(StatusEnum.MISS_DEADLINE.getStatus());
                programRepository.save(program);
            }
        }
    }


}
