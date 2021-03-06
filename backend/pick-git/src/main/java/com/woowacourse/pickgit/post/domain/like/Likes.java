package com.woowacourse.pickgit.post.domain.like;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Embeddable
public class Likes {

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<Like> likes = new ArrayList<>();

    protected Likes() {
    }

    public int getCounts() {
        return likes.size();
    }

    public boolean contains(String userName) {
        return likes.stream()
            .anyMatch(like -> like.contains(userName));
    }
}
