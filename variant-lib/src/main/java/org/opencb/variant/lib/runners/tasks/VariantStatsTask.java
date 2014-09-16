package org.opencb.variant.lib.runners.tasks;

import java.io.IOException;
import java.util.List;
import org.opencb.biodata.formats.variant.io.VariantReader;
import org.opencb.biodata.models.variant.ArchivedVariantFile;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.stats.ArchivedVariantFileStats;
import org.opencb.biodata.models.variant.stats.VariantAggregatedStats;
import org.opencb.biodata.models.variant.stats.VariantStats;
import org.opencb.commons.run.Task;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class VariantStatsTask extends Task<Variant> {

    private VariantReader reader;
    private VariantSource study;
    private ArchivedVariantFileStats stats;

    public VariantStatsTask(VariantReader reader, VariantSource study) {
        super();
        this.reader = reader;
        this.study = study;
        stats = new ArchivedVariantFileStats(study.getFileId(), study.getStudyId());
    }

    public VariantStatsTask(VariantReader reader, VariantSource study, int priority) {
        super(priority);
        this.reader = reader;
        this.study = study;
        stats = new ArchivedVariantFileStats(study.getFileId(), study.getStudyId());
    }

    @Override
    public boolean apply(List<Variant> batch) throws IOException {
//        VariantStats.calculateStatsForVariantsList(batch, study.getPedigree());
        for (Variant variant : batch) {
            for (ArchivedVariantFile file : variant.getFiles().values()) {
                VariantStats variantStats = null;
                switch (study.getAggregation()) {
                    case NONE:
                        variantStats = new VariantStats(variant);
                        break;
                    case BASIC:
                        variantStats = new VariantAggregatedStats(variant);
                        break;
                    case EVS:
                        break;
                }
                file.setStats(variantStats.calculate(file.getSamplesData(), file.getAttributes(), study.getPedigree()));
            }
        }
        
        stats.updateFileStats(batch);
        stats.updateSampleStats(batch, study.getPedigree());
        return true;
    }

    @Override
    public boolean post() {
        study.setStats(stats.getFileStats());
        return true;
    }
}
