package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Review;

import javax.persistence.*;

@Entity
@Table(name = "images")
public class Image implements DomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String url;
    private Integer width;

    @OneToOne
    @JoinColumn(name="question_id")
    private Question question;

    @OneToOne
    @JoinColumn(name="review_id")
    private Review review;

    public Image() {}

    public Image(ImageDto imageDto) {
        this.url = imageDto.getUrl();
        this.width = imageDto.getWidth();
    }

    public Review getReview() { return review; }

    public void setReview(Review review) { this.review = review; }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitImage(this);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    @Override
    public String toString() {
        return "Image{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", width=" + width +
                '}';
    }
}
