package com.kuney.community.config;

import com.kuney.community.job.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @author kuneychen
 * @since 2022/7/6 21:28
 */
@Configuration
public class QuartzConfiguration {

    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean simpleTriggerFactoryBean(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
        triggerFactoryBean.setName("postScoreRefreshTrigger");
        triggerFactoryBean.setGroup("communityTriggerGroup");
        triggerFactoryBean.setJobDetail(postScoreRefreshJobDetail);
        triggerFactoryBean.setJobDataMap(new JobDataMap());
        // triggerFactoryBean.setRepeatInterval(1000 * 60 * 5); // 测试环境：每5分钟执行一次
        triggerFactoryBean.setRepeatInterval(1000 * 60 * 60 * 2); // 每2小时执行一次
        return triggerFactoryBean;
    }

}
