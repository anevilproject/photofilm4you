package edu.uoc.pdp.core.ejb.media.handler;

import edu.uoc.pdp.core.dao.CustomerDAO;
import edu.uoc.pdp.core.dao.ProductDAO;
import edu.uoc.pdp.core.dao.ProductRatingDAO;
import edu.uoc.pdp.core.ejb.session.SessionManager;
import edu.uoc.pdp.db.entity.ProductRating;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Singleton
public class RatingHandler {

    @Inject
    private ProductRatingDAO productRatingDAO;
    @Inject
    private ProductDAO productDAO;
    @Inject
    private CustomerDAO customerDAO;
    @Inject
    private SessionManager sessionManager;

    /**
     * rate & comment a product
     *
     * @param productId the productId of the product to be rated
     * @param rate      the rate (from 0 to 5)
     * @param comment   the comment to add
     * @return a ProductRating entity persisted in the database and its Product related with its rating average field updated
     */
    @Transactional
    public ProductRating rateProduct(String productId, Integer rate, String comment) {
        ProductRating rating = new ProductRating();
        rating.setRating(rate);
        rating.setComment(comment);
        rating.setProduct(productDAO.getById(productId));
        rating.setCustomer(customerDAO.getByEmail(sessionManager.getUserId()));
        rating.setCreated(LocalDateTime.now());

        ProductRating result = productRatingDAO.save(rating);
        productDAO.updateRating(productId);

        return result;
    }

    public List<ProductRating> listAllProductComments(String productId) {
        return productRatingDAO.findCommentsByProduct(productId);
    }
}
