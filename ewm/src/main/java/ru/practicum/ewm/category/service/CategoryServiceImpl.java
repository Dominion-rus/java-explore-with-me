package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.stats.dto.exceptions.ConflictException;
import ru.practicum.stats.dto.exceptions.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto create(NewCategoryDto dto) {
        if (repository.existsByName(dto.getName())) {
            throw new ConflictException("Category with this name already exists");
        }
        Category saved = repository.save(CategoryMapper.toEntity(dto));
        return CategoryMapper.toDto(saved);
    }

    @Override
    public CategoryDto update(Long id, CategoryDto dto) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        if (repository.existsByName(dto.getName()) &&
                !category.getName().equals(dto.getName())) {
            throw new ConflictException("Category name already exists");
        }
        category.setName(dto.getName());
        return CategoryMapper.toDto(repository.save(category));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Category not found");
        }

        if (eventRepository.existsByCategoryId(id)) {
            throw new ConflictException("Cannot delete category with assigned events");
        }

        repository.deleteById(id);
    }

    @Override
    public List<CategoryDto> findAll(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return repository.findAll(pageable).stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto findById(Long id) {
        return repository.findById(id)
                .map(CategoryMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }
}
