package com.husky.cloud.config;

import com.husky.cloud.entity.ScheduleJob;
import org.quartz.*;

/**
 * 定时器工具类
 */
public class ScheduleUtil {

    private ScheduleUtil (){}

    private static final String SCHEDULE_NAME = "CICADA_" ;

    /**
     * 触发器 KEY
     */
    public static TriggerKey getTriggerKey(Long jobId){
        return TriggerKey.triggerKey(SCHEDULE_NAME+jobId) ;
    }

    /**
     * 定时器 Key
     */
    public static JobKey getJobKey (Long jobId){
        return JobKey.jobKey(SCHEDULE_NAME+jobId) ;
    }

    /**
     * 表达式触发器
     */
    public static CronTrigger getCronTrigger (Scheduler scheduler,Long jobId){
        try {
            return (CronTrigger)scheduler.getTrigger(getTriggerKey(jobId)) ;
        } catch (SchedulerException e){
            throw new RuntimeException("getCronTrigger Fail",e) ;
        }
    }

    /**
     * 创建定时器
     */
    public static void createJob (Scheduler scheduler, ScheduleJob scheduleJob){
        try {
            // 构建定时器
            JobDetail jobDetail = JobBuilder.newJob(TaskJobLog.class).withIdentity(getJobKey(scheduleJob.getId())).build() ;
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                    .cronSchedule(scheduleJob.getCronExpression())
                    .withMisfireHandlingInstructionDoNothing() ;
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(getTriggerKey(scheduleJob.getId()))
                    .withSchedule(scheduleBuilder).build() ;
            jobDetail.getJobDataMap().put(ScheduleJob.JOB_PARAM_KEY,scheduleJob);
            scheduler.scheduleJob(jobDetail,trigger) ;
            // 如果该定时器处于暂停状态
            if (scheduleJob.getStatus() == 1){
                pauseJob(scheduler,scheduleJob.getId()) ;
            }
        } catch (SchedulerException e){
            throw new RuntimeException("createJob Fail",e) ;
        }
    }

    /**
     * 更新定时任务
     */
    public static void updateJob(Scheduler scheduler, ScheduleJob scheduleJob) {
        try {
            // 构建定时器
            TriggerKey triggerKey = getTriggerKey(scheduleJob.getId());
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression())
                    .withMisfireHandlingInstructionDoNothing();
            CronTrigger trigger = getCronTrigger(scheduler, scheduleJob.getId());
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            trigger.getJobDataMap().put(ScheduleJob.JOB_PARAM_KEY, scheduleJob);
            scheduler.rescheduleJob(triggerKey, trigger);
            // 如果该定时器处于暂停状态
            if(scheduleJob.getStatus() == 1){
                pauseJob(scheduler, scheduleJob.getId());
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("updateJob Fail",e) ;
        }
    }

    /**
     * 停止定时器
     */
    public static void pauseJob (Scheduler scheduler,Long jobId){
        try {
            scheduler.pauseJob(getJobKey(jobId));
        } catch (SchedulerException e){
            throw new RuntimeException("pauseJob Fail",e) ;
        }
    }

    /**
     * 恢复定时器
     */
    public static void resumeJob (Scheduler scheduler,Long jobId){
        try {
            scheduler.resumeJob(getJobKey(jobId));
        } catch (SchedulerException e){
            throw new RuntimeException("resumeJob Fail",e) ;
        }
    }

    /**
     * 删除定时器
     */
    public static void deleteJob (Scheduler scheduler,Long jobId){
        try {
            scheduler.deleteJob(getJobKey(jobId));
        } catch (SchedulerException e){
            throw new RuntimeException("deleteJob Fail",e) ;
        }
    }

    /**
     * 执行定时器
     */
    public static void run (Scheduler scheduler, ScheduleJob scheduleJob){
        try {
            JobDataMap dataMap = new JobDataMap() ;
            dataMap.put(ScheduleJob.JOB_PARAM_KEY,scheduleJob);
            scheduler.triggerJob(getJobKey(scheduleJob.getId()),dataMap);
        } catch (SchedulerException e){
            throw new RuntimeException("run Fail",e) ;
        }
    }
}