package interfaces;

import exception.DatabaseException;
import java.util.List;

/**
 * Generic Interface for CRUD Operations in Data Access Objects.
 * @param <T> Model type
 */
public interface CRUDOperations<T> {

    boolean add(T entity) throws DatabaseException;

    T getById(int id) throws DatabaseException;

    List<T> getAll() throws DatabaseException;

    boolean update(T entity) throws DatabaseException;

    boolean delete(int id) throws DatabaseException;
}
