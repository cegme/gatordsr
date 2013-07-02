import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import edu.ufl.cise.pipeline.Entity;

/**
 * This class will keep track of the statistics of the KBA runs that we had for
 * the first submission. <br>
 * 
 * We want to c compute the following metrics:<br>
 * <br>
 * 1. For each entity, how many documents (with lingpipe) matched the alias
 * stage?<br>
 * <br>
 * 2. For each entity, "how many sentences" matched the filter stage (ie, before
 * running the extraction rules, but after filtering at the sentence level)?<br>
 * <br>
 * 3. For each entity, how many sentences have slot values (ie, final output)?
 * We already generate the slot values as soon as we find them. <br>
 * <br>
 * 4. For each entity, how many documents with only clean_visible match the
 * alias stage (ie, these documents we skipped for the submission)? needs to be
 * doen b.c. the file that chris gives with cpp script only talks about gpg
 * filesand I skip non-lingpipe documents, I need to look at ling pipe ones.
 * Specifying where we find the match in the outputformat is the process that we
 * take.<br>
 * <br>
 * <br>
 * Important -- only count unique (entity, sentence) pairs. If a sentences (or
 * document) is processed multiple times per entity for some reason (ie. bug),
 * keep count of the duplicate processing (for each entity).
 * 
 * @author morteza
 * 
 */
public class FirstSubmissionStatisticsGenerator {

	/**
	 * HashMap will do the work as we will not update the map but only update the
	 * counts which are atomic ooperators themeslves.
	 */
//	public final Hashtable<Entity, AtomicLong>	ALIAS_STAGE_MATCHED_DOCUMENT_WITH_LINGPIPE_HM;
//
//	public AtomicLong														a	= new AtomicLong(0);
//
//	public FirstSubmissionStatisticsGenerator(List<Entity> listEntity) {
//		ALIAS_STAGE_MATCHED_DOCUMENT_WITH_LINGPIPE_HM = new Hashtable<Entity, AtomicLong>();
//		for (Entity e : listEntity) {
//			ALIAS_STAGE_MATCHED_DOCUMENT_WITH_LINGPIPE_HM.put(e, new AtomicLong(0));
//		}
//	}
//
//	public static void main(String[] args) {
//
//	}

}
