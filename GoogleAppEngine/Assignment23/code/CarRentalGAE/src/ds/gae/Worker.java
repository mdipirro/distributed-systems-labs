package ds.gae;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ds.gae.entities.Quote;
import ds.gae.entities.QuotesStatus;

public class Worker extends HttpServlet {
	private static final long serialVersionUID = -7058685883212377590L;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		EntityManager em = EMF.get().createEntityManager();
		ObjectInputStream ois = new ObjectInputStream(req.getInputStream());
		QuotesStatus status = null;
		try {
			Payload payload = (Payload) ois.readObject();
			status = em.find(QuotesStatus.class, payload.getID());
			if (status != null) {
				status.setStatus(QuotesStatus.Status.IN_PROCESSING);
				confirmQuotes(payload.getQuotes()); // if throws an exception, set status to FAILED
				status.setStatus(QuotesStatus.Status.CONFIRMED);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			em.close();
			ois.close();
		}
	}
	
	private void confirmQuotes(List<Quote> quotes) { 
		// TODO to implement
	}
}
