package edu.uoc.pdp.web.bean.catalog.front;

import edu.uoc.pdp.core.ejb.catalog.CatalogFacade;
import edu.uoc.pdp.core.ejb.media.MediaFacade;
import edu.uoc.pdp.db.entity.Product;
import edu.uoc.pdp.db.entity.ProductRating;
import edu.uoc.pdp.web.bean.BaseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

import static edu.uoc.pdp.core.utils.DateUtils.formatPublishDate;

@ViewScoped
@ManagedBean(name = "frontProductBean")
public class FrontProductBean extends BaseBean {

    private static final long serialVersionUID = -8098012408850780669L;

    private static final Logger log = LoggerFactory.getLogger(FrontProductBean.class);

    @EJB
    private CatalogFacade catalogFacade;
    @EJB
    private MediaFacade mediaFacade;

    private Product product;
    private String productId;
    private List<ProductRating> ratings;
    @Max(value = 10, message = "La puntuació ha d'estar entre 0 i 10")
    @Min(value = 0, message = "La puntuació ha d'estar entre 0 i 10")
    @NotNull(message = "Es obligatori especificar una puntuació")
    private Integer rating;
    @Size(max = 1000, message = "El comentari no pot tenir més de 1000 caràcters")
    private String comment;


    /**
     * Rates a product
     */
    public void rate() {
        try {
            mediaFacade.rateProduct(productId, rating, comment);
            addGlobalSuccessMessage("Valoració enviada!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addGlobalErrorMessage("No s'ha pogut enviar la valoració.");
        }
        redirect();
    }

    /**
     * Formats the published date of a review using {@link edu.uoc.pdp.core.utils.DateUtils#formatPublishDate(LocalDateTime)}
     *
     * @param date Date to be formatted
     * @return The formatted date
     */
    public String formatReviewDate(LocalDateTime date) {
        return formatPublishDate(date);
    }

    /**
     * Returns a product's rating converted to a percentage value to be rendered by the view
     *
     * @param product Product
     * @return The product's rating as a percentage
     */
    public int resolveProductPercentage(Product product) {
        return resolveStarPercentage(product.getRating());
    }

    /**
     * Returns a product's review rating converted to a percentage value to be rendered by the view
     *
     * @param rating Product rating
     * @return The product's review rating as a percentage
     */
    public int resolveRatingPercentage(ProductRating rating) {
        return resolveStarPercentage(rating.getRating());
    }

    public List<ProductRating> getRatings() {
        if (ratings == null) {
            ratings = mediaFacade.listAllProductComments(productId);
        }
        return ratings;
    }

    public Product getProduct() {
        if (product == null) {
            product = catalogFacade.getProduct(productId);
        }
        return product;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getRating() {
        if (rating == null) {
            rating = 0;
        }
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private int resolveStarPercentage(Number number) {
        if (number == null) {
            return 0;
        }
        // products are rated over 10 in the persistence layer but displayed over 5 for simplicity
        float value = number.floatValue() / 2f;

        return Math.round(value / 5 * 100) / 5 * 5;
    }
}
