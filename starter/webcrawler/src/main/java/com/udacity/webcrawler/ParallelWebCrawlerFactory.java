package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

public class ParallelWebCrawlerFactory {
  private final Clock clock;
  private final PageParserFactory parserFactory;
  private int maxDepth;
  private final List<Pattern> ignoredUrls;
  private final Instant deadline;
  private final Map<String, Integer> counts;
  private final Set<String> visitedUrls;

  @Inject
  ParallelWebCrawlerFactory(
      Clock clock,
      PageParserFactory parserFactory,
      @MaxDepth int maxDepth,
      @IgnoredUrls List<Pattern> ignoredUrls,
      Instant deadline,
      Map<String, Integer> counts,
      Set<String> visitedUrls
  ) {
    this.clock = clock;
    this.parserFactory = parserFactory;
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
    this.deadline = deadline;
    this.counts = counts;
    this.visitedUrls = visitedUrls;
  }

  public ParallelCrawlTask create(String url) {
    return new ParallelCrawlTask(url);
  }

  public class ParallelCrawlTask extends RecursiveAction {
    private final String url;

    public ParallelCrawlTask(String url) {
      this.url = url;
    }

    private void crawlInternal(
        String url
    ) {
      if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
        return;
      }
      for (Pattern pattern : ignoredUrls) {
        if (pattern.matcher(url).matches()) {
          return;
        }
      }
      if (visitedUrls.contains(url)) {
        return;
      }
      visitedUrls.add(url);
      PageParser.Result result = parserFactory.get(url).parse();
      for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
        if (counts.containsKey(e.getKey())) {
          counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
        } else {
          counts.put(e.getKey(), e.getValue());
        }
      }
      for (String link : result.getLinks()) {
        maxDepth -= 1;
        crawlInternal(link);
      }
    }

    @Override
    protected void compute() {
      crawlInternal(url);
    }
  }
}
