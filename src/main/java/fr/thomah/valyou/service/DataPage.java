package fr.thomah.valyou.service;

import fr.thomah.valyou.entity.model.GenericModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class DataPage<T> implements Page<T> {

    private List<T> content = new ArrayList<>();
    private int totalPages;
    private long totalElements;
    private int number;
    private int size;
    private int numberOfElements;
    private Sort sort;

    public DataPage(Page<? extends GenericModel> entities) {
        totalPages = entities.getTotalPages();
        totalElements = entities.getTotalElements();
        number = entities.getNumber();
        size = entities.getSize();
        numberOfElements = entities.getNumberOfElements();
        sort = entities.getSort();
    }

    @Override
    public boolean hasContent() {
        return content.size() > 0;
    }

    @Override
    public boolean isFirst() {
        return number - 1 == 0;
    }

    @Override
    public boolean isLast() {
        return number + 1 == totalPages;
    }

    @Override
    public boolean hasNext() {
        return number + 1 < totalPages;
    }

    @Override
    public boolean hasPrevious() {
        return number - 1 > 0;
    }

    @Override
    public Pageable nextPageable() {
        return null;
    }

    @Override
    public Pageable previousPageable() {
        return null;
    }

    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> function) {
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }

}
