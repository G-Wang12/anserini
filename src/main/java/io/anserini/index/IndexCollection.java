/**
 * Anserini: An information retrieval toolkit built on Lucene
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.anserini.index;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.kohsuke.args4j.ParserProperties;

import java.util.concurrent.TimeUnit;

public final class IndexCollection {
  private static final Logger LOG = LogManager.getLogger(IndexCollection.class);

  public static void main(String[] args) throws Exception {
    IndexArgs indexArgs = new IndexArgs();

    CmdLineParser parser = new CmdLineParser(indexArgs, ParserProperties.defaults().withUsageWidth(90));

    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      parser.printUsage(System.err);
      System.err.println("Example: "+ IndexCollection.class.getSimpleName() + parser.printExample(OptionHandlerFilter.REQUIRED));
      return;
    }

    if (indexArgs.docvectors && !indexArgs.positions) {
      LOG.warn("to store docVectors you must store positions too. With this configuration, both positions and docVectors will not be stored!");
    }

    final long start = System.nanoTime();
    MultithreadedIndexer indexer = new MultithreadedIndexer(indexArgs);

    LOG.info("Index path: " + indexArgs.index);
    LOG.info("Threads: " + indexArgs.threads);
    LOG.info("Keep Stopwords: " + indexArgs.keepstop);
    LOG.info("Positions: " + indexArgs.positions);
    LOG.info("Store docVectors: " + indexArgs.docvectors);
    LOG.info("Optimize (merge segments): " + indexArgs.optimize);

    LOG.info("Starting indexer...");

    int numIndexed = indexer.run();
    final long durationMillis = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
    LOG.info("Total " + numIndexed + " documents indexed in " +
        DurationFormatUtils.formatDuration(durationMillis, "HH:mm:ss"));
  }
}