package team6.sobun.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import team6.sobun.domain.post.dto.PostResponseDto;
import team6.sobun.domain.post.dto.PostSearchCondition;

public interface PostRepositoryCustom {

    Page<PostResponseDto> serachPostByPage(PostSearchCondition condition, Pageable pageable);

    Page<PostResponseDto> searchPostByPageByPopularity(PostSearchCondition condition, Pageable pageable);

    Page<PostResponseDto> searchPostByPageByMostView(PostSearchCondition condition, Pageable pageable);

}
