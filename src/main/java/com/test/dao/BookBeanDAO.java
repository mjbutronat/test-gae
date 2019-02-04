package com.test.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.search.*;
import com.googlecode.objectify.ObjectifyService;
import com.test.data.BookBean;


public class BookBeanDAO {

	private static final Logger LOGGER = Logger.getLogger(BookBeanDAO.class.getName());
	private static final String SEARCHINDEX = "searchIndex";

    /**
     * @return list of book beans
     */
    public List<BookBean> list() {
        LOGGER.info("Retrieving list of books");
        return ObjectifyService.ofy().load().type(BookBean.class).order("author").list();
    }

    /**
     * @param id
     * @return book bean with given id
     */
    public BookBean get(Long id) {
        LOGGER.info("Retrieving book " + id);
        return ObjectifyService.ofy().load().type(BookBean.class).id(id).now();
    }

    /**
     * Saves given bean
     * @param bean
     */
    public void save(BookBean bean) {
        if (bean == null) {
            throw new IllegalArgumentException("null book object");
        }
        LOGGER.info("Saving book " + bean.getId());
        ObjectifyService.ofy().save().entity(bean).now();
        
        try{
            Document doc = Document.newBuilder()
                    .setId(bean.getId().toString())
                    .addField(Field.newBuilder().setName("name").setText(getPartString(bean.getName())))
                    .addField(Field.newBuilder().setName("author").setText(getPartString(bean.getAuthor())))
                    .addField(Field.newBuilder().setName("year").setNumber(bean.getYear()))
                    .addField(Field.newBuilder().setName("gender").setText(bean.getGender()))
                    .build();
            
            IndexSpec indexSpec = IndexSpec.newBuilder().setName(SEARCHINDEX).build();
            Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
            
            index.put(doc);
            
        } catch (PutException e) {
            throw e;
        }
    }

    /**
     * Deletes given bean
     * @param bean
     */
    public void delete(BookBean bean) {
        if (bean == null) {
            throw new IllegalArgumentException("null book object");
        }
        LOGGER.info("Deleting book " + bean.getId());
        ObjectifyService.ofy().delete().entity(bean);
    }
    
    /**
     * Search book
     * @param criteria
     * @return
     */
    public List<BookBean> search(String criteria) {
    	List<BookBean> listBook = new ArrayList<BookBean>();
        
        LOGGER.info("Retrieving list book by query");
        
        Query query = Query.newBuilder().build(criteria);
        
        IndexSpec indexSpec = IndexSpec.newBuilder().setName(SEARCHINDEX).build();
        Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
        
        Results<ScoredDocument> results = index.search(query);
        
        for (ScoredDocument doc : results) {
            BookBean book = get(Long.valueOf(doc.getId()));
            listBook.add(book);
        }
        
        return listBook;
    }
    
    /**
     * Get parts
     * @param str
     * @return
     */
    private String getPartString(String str){

        List<String> parts = new ArrayList<String>();
        
        for (String word : str.split("\\s+")){
            int cont = 1;
            while(true){
                parts.add(word.substring(0, cont));
                if (cont == word.length()){ break; }
                cont++;
            }
        }
        
        return parts.toString().substring(1, parts.toString().length()-1);
    }

    
}
