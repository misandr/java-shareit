package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {
    List<Item> findByOwner(User owner);

    Page<Item> findByOwner(User owner, Pageable pageable);

    List<Item> findByRequestId(Long requestId);

    List<Item> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs(String text1, String text2, Boolean status);

    Page<Item> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIs(String text1, String text2, Boolean status, Pageable pageable);
}

