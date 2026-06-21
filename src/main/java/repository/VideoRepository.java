package repository;

import model.VideoMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**Interacts with local DB*/

@Repository
public interface VideoRepository extends JpaRepository<VideoMetadata, String> {
    ///TODO: Add search methods:
    List<VideoMetadata> findByTitleContainingIgnoreCase(String title);

}
