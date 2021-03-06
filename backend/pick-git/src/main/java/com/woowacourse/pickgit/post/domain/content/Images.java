package com.woowacourse.pickgit.post.domain.content;

import static java.util.stream.Collectors.toList;

import com.woowacourse.pickgit.post.domain.Post;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Embeddable
public class Images {

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<Image> images = new ArrayList<>();

    protected Images() {
    }

    public Images(List<Image> images) {
        this.images = images;
    }

    public List<String> getUrls() {
        return images.stream()
            .map(Image::getUrl)
            .collect(toList());
    }
    public List<String> getImageUrls() {
        return images.stream()
            .map(Image::getUrl)
            .collect(Collectors.toList());
    }

    public void setMapping(Post post) {
        images.forEach(image -> image.toPost(post));
    }
}
