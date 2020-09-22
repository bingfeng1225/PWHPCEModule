package cn.haier.bio.medical.hpce;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.qd.peiwen.pwtools.EmptyUtils;

public class ProgramEntity {
    private int id;
    private String name;
    private String author;
    private Date updateTime;
    private List<ProgramTaskEntity> tasks;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getTasksCount() {
        if (EmptyUtils.isEmpty(this.tasks)) {
            return 0;
        }
        return tasks.size() + 1;
    }

    public List<ProgramTaskEntity> getTasks() {
        return tasks;
    }

    public void addTask(ProgramTaskEntity task) {
        if (EmptyUtils.isEmpty(this.tasks)) {
            this.tasks = new ArrayList<>();
        }
        this.tasks.add(task);
    }

    public void setTasks(List<ProgramTaskEntity> tasks) {
        this.tasks = tasks;
    }

    public ProgramTaskEntity findTaskByIndex(int index) {
        ProgramTaskEntity task = null;
        for (ProgramTaskEntity item : this.tasks) {
            if (index == item.getIndex()) {
                task = item;
                break;
            }
        }
        return task;
    }
}
