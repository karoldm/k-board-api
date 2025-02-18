package com.karoldm.k_board_api.utils;

import com.karoldm.k_board_api.entities.Task;
import com.karoldm.k_board_api.enums.TaskStatus;

import java.util.Objects;
import java.util.Set;

public class ProjectProgress {
    private ProjectProgress(){}

    public static double calculeProgress(Set<Task> tasks) {
        if(tasks.isEmpty()) return 0.0;

        int completedCount = 0;

        for(Task task : tasks) {
            if(Objects.equals(task.getStatus(), TaskStatus.COMPLETED.toString())){
                completedCount++;
            }
        }

        return (double)completedCount / (double)tasks.size();
    }
}
