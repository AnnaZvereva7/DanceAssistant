package com.example.ZverevaDanceWCS.service.model.subscriptions;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.subscriptions.dto.SubscriptionTypeNewDTO;
import com.example.ZverevaDanceWCS.service.model.subscriptions.dto.SubscriptionTypeStringDTO;
import com.example.ZverevaDanceWCS.service.model.user.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionTypeService {
    private final SubscriptionTypeRepository subscriptionTypeRepository;

    public SubscriptionTypeService(SubscriptionTypeRepository subscriptionTypeRepository) {
        this.subscriptionTypeRepository = subscriptionTypeRepository;
    }

    public List<SubscriptionType> getSubscriptionTypesByTrainer(Long trainerId) {
        List<SubscriptionType> subscriptionTypes = subscriptionTypeRepository.findByTrainerIdAndActive(trainerId, true);
        return subscriptionTypes;

    }

    public void saveNewSubscriptionType(SubscriptionTypeNewDTO subscriptionTypeNewDTO, User trainer) {
        SubscriptionType subscriptionType = new SubscriptionType();
        subscriptionType.setTrainerId(trainer.getId());
        subscriptionType.setTrainerName(trainer.getName());
        subscriptionType.setDurationMonths(subscriptionTypeNewDTO.getDurationMonths());
        subscriptionType.setAmountLessons(subscriptionTypeNewDTO.getAmountLessons());
        subscriptionType.setPrice(subscriptionTypeNewDTO.getPrice());
        subscriptionType.setActive(true);
        subscriptionType.setComment("Created at " + java.time.LocalDateTime.now().format(Constant.formatterJustDate));
        subscriptionTypeRepository.save(subscriptionType);
    }

    public void deleteSubscriptionType (Long subscriptionTypeId, User trainer) {
        SubscriptionType subscriptionType = subscriptionTypeRepository.findById(subscriptionTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription type not found"));
        if (!subscriptionType.getTrainerId().equals(trainer.getId())) {
            throw new IllegalArgumentException("You are not authorized to delete this subscription type");
        }
        subscriptionType.setActive(false);
        subscriptionType.setComment(subscriptionType.getComment() + " Deactivated at " + java.time.LocalDateTime.now().format(Constant.formatterJustDate));
        subscriptionTypeRepository.save(subscriptionType);
    }

    public SubscriptionType findById(Long subscriptionTypeId) {
        return subscriptionTypeRepository.findById(subscriptionTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription type not found"));
    }
}
