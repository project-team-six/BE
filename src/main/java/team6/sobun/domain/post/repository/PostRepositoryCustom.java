package team6.sobun.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import team6.sobun.domain.post.dto.PostResponseDto;
import team6.sobun.domain.post.dto.PostSearchCondition;
import team6.sobun.domain.post.entity.Post;

import java.util.List;

public interface PostRepositoryCustom {

    Page<PostResponseDto> searchPostByPage(PostSearchCondition condition, Pageable pageable);


}
