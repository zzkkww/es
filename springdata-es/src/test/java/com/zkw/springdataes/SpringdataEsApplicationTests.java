package com.zkw.springdataes;

import com.zkw.springdataes.mapper.UserEsRepository;
import com.zkw.springdataes.pojo.User;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.io.IOException;
import java.util.*;

@SpringBootTest
class SpringdataEsApplicationTests {

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	@Autowired
	private ElasticsearchOperations elasticsearchOperations;

	@Autowired
	private UserEsRepository userEsRepository;


	@Test
	void contextLoads() {
		System.out.println("哈哈哈");
	}

	@Test
	void testESClient() throws IOException {
		GetRequest getRequest = new GetRequest("zkw_index", "1");
		GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);

		System.out.println(getResponse.toString());

//		CreateIndexRequest request = new CreateIndexRequest("zkw_index");
//		restHighLevelClient.indices().create(request,RequestOptions.DEFAULT);

	}

	@Test
	public void testSave() {

		User user = new User();
		user.setLastName("张三");
		user.setAge(29);
		user.setBirthDate(new Date());
		user.setId("1");
		user.setIsDeleted(false);
		user.setCreateTime(new Date());
		user.setUpdateTime(new Date());

		IndexCoordinates indexCoordinates = elasticsearchOperations.getIndexCoordinatesFor(user.getClass());

		IndexQuery indexQuery = new IndexQueryBuilder()
				.withId(user.getId())
				.withObject(user)
				.build();
		String documentId = elasticsearchOperations.index(indexQuery, indexCoordinates);
	}

	@Test
	public void testQuery() {
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
				.withQuery(new MatchAllQueryBuilder())
				.build();

		SearchHits<User> searchHits = elasticsearchOperations.search(searchQuery, User.class);
		long count = searchHits.getTotalHits();
		System.out.println(count);
		List<SearchHit<User>> list = searchHits.getSearchHits();
		for (SearchHit hit:list) {
			System.out.println(hit.getContent());
		}
	}

	@Test
	public void testMatchQuery() {
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
				.withQuery(new MatchAllQueryBuilder())
				.build();

		SearchHits<User> searchHits = elasticsearchOperations.search(searchQuery, User.class);
		long count = searchHits.getTotalHits();
		System.out.println(count);
		List<SearchHit<User>> list = searchHits.getSearchHits();
		for (SearchHit hit:list) {
			System.out.println(hit.getContent());
		}
	}

	/**
	 * 测试ElasticsearchRepository<实体类，string></>
	 */
	@Test
	public void testSaveElasticsearchRepository() {
		User entity = new User();
		entity.setId(UUID.randomUUID().toString());
		entity.setAge(50);
		entity.setLastName("东野圭吾");
		entity.setBirthDate(new Date());
		entity.setCreateTime(new Date());
		entity.setUpdateTime(new Date());
		entity.setIsDeleted(false);
		userEsRepository.save(entity);
	}

	@Test
	public void testFindAll() {
		long count = userEsRepository.count();
		System.out.println(count);
		Iterable<User> result = userEsRepository.findAll();
		Iterator<User> data = result.iterator();
		while (data.hasNext()) {
			System.out.println(data.next());

		}
	}

	@Test
	public void testFindById() {
		Optional<User> data = userEsRepository.findById("c686c8d9-474d-4144-9c1e-e51f74cfc90f");
		System.out.println(data.get());
	}



}
