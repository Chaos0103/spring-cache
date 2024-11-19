# Spring Cache Practice Project

---

# Spring Cache

Spring은 일부 데이터를 미리 메모리 저장소에 저장하고 저장된 데이터를 다시 읽어 사용하는 캐시 기능을 제공한다. 트랜잭션과 마찬가지로 AOP를 사용하여 캐시 기능을 구현하였고, 캐시 어노테이션을 사용하면 쉽게 구현할 수 있다. Spring에서 **캐시 데이터를 관리하는 기능은 별도의 캐시 프레임워크에 위임**한다.

## 캐시 저장소를 구성하는 방식

### 1. 로컬 캐시

> **Java 애플리케이션에 embedded하는 방식**
>
- 애플리케이션은 **각각의 캐시 시스템**을 가지며, 1:1 방식으로 사용한다.
  - 데이터를 서로 공유할 수 없다.
  - 같은 이름의 데이터라도 각 서버마다 관리하고 있는 캐시 데이터는 다르다.

### 2. 원격 캐시

> **애플리케이션 외부의 독립 메모리 저장소를 별도로 구축하여 모든 인스턴스가 네트워크를 사용하여 데이터를 캐시하는 방식**
>
- **외부에 독립적인 데이터 저장소**를 사용한다.
  - 데이터를 캐시하거나 사용하면 I/O가 발생한다.
    - 로컬 캐시 방식보다 I/O 시간만큼 서버 리소스와 시간이 더 소요된다.
  - 네트워크를 사용하므로 외부 환경으로 캐시 성능이 영향을 받는다.
  - 어떤 서버라도 모두 같은 데이터를 사용할 수 있고, 일관된 방식으로 데이터를 읽고 쓸 수 있다.

# Cache Manager 구현체 종류

- `ConcurrentMapCacheManager`
  - JRE에서 제공하는 `ConcurrentHashMap`을 캐시 저장소로 사용할 수 있는 구현체이다.
  - 캐시 정보를 `Map` 타입으로 메모리에 저장해두기 때문에 빠르고 별다른 설정이 필요없다는 장점이 있지만, 실제 서비스에서 사용하기엔 기능이 빈약하다.
- `SimpleCacheManager`
  - 기본적으로 제공하는 캐시가 없다.
  - 사용할 캐시를 직접 등록하여 사용하기 위한 캐시 매니저 구현체이다.
- `EnCacheCacheManager`
  - Java에서 유명한 캐시 프레임워크 중 하나인 EnCache를 지원하는 캐시 매니저 구현체이다.
- `CaffeineCacheManager`
  - Java 8로 Guava 캐시를 재작성한 Caffeine 캐시 저장소를 사용할 수 있는 구현체이다.
  - EnCache와 함께 인기 있는 매니저인데, 이보다 좋은 성능을 가진다.
- `JCacheCacheManager`
  - JSR-107 표준을 따르는 JCache 캐시 저장소를 사용할 수 있는 구현체이다.
- `RedisCacheManager`
  - Redis를 캐시 저장소로 사용할 수 있는 구현체이다.
- `CompositeCacheManager`
  - 한 개 이상의 캐시 매니저를 사용할 수 있는 혼합 캐시 매니저이다.

# 의존성 추가

```
implementation 'org.springframework.boot:spring-boot-starter-cache'
```

# @EnableCaching

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory basicCacheRedisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public CacheManager cacheManager() {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1)) //캐시에 적용할 TTL(Time To Live) 설정
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())) //캐시 키의 직렬화/역직렬화에 사용되는 값을 정의
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class))); //캐시 값의 직렬화/역직렬화에 사용되는 값을 정의

        Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>();
        configurationMap.put("itemCache", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.RedisCacheManagerBuilder
            .fromConnectionFactory(basicCacheRedisConnectionFactory())
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(configurationMap) //미리 추가할 캐시 정보
            .build();
    }
}
```

# @Cacheable

- 캐시 저장소에 **캐시 데이터를 저장하거나 조회하는 기능**을 사용할 수 있다.
- 어노테이션이 정의된 메서드를 실행하면 데이터 저장소에 캐시 데이터 유무를 확인한다.
- 적용된 메서드의 리턴 값을 기준으로 캐시에 값을 저장한다.
- 캐시에 데이터가 있다면 메서드를 실행하지 않고 바로 데이터를 리턴한다. 만약 예외가 발생하면 캐시 데이터는 저장하지 않는다.

| 속성 | 설명 | Type |
| --- | --- | --- |
| cacheName | 캐시 이름(설정 메서드 리턴값이 저장되는) | String[] |
| value | cacheName의 별칭(alias) | String[] |
| key | 동적인 키 값을 사용하는 SpEL표현식. 동일한 cacheName을 사용하지만 구분될 필요가 있을때 사용 | String |
| condition | SpEL 표현식이 참인 경우에만 캐싱 적용. or, and 등 조건식 및 논리연산 가능 | String |
| unless | 캐싱을 막기 위해 사용되는 SpEL 표현식. condition과 반대로 참일 경우에만 캐싱이 적용되지 않음 | String |
| cacheManager | 사용할 CacheManager 지정 | String |
| sync | 여러 스레드가 동일한 키에 대한 값을 로드하려고 할 경우, 기본 메서드의 호출을 동기화한다. 캐시 구현체가 Thread safe 하지 않는 경우, 캐시에 동기화를 걸 수 있는 속성 | boolean |

## 적용 예제

```java
@Cacheable(value = "itemCache")
public Item searchItem(Long itemId) {
    log.info("ItemService 호출 [itemId = {}]", itemId);
    return itemRepository.findById(itemId)
        .orElseThrow(IllegalArgumentException::new);
}
```

- `Long itemId` 메서드 인자의 `toString()` 메서드를 사용하여 캐시 키를 설정한다. 인자가 여러개라면 모두 조합하여 캐시 키를 설정한다.
  - `searchItem(Long itemId)`: itemCache::1
  - `searchItem(Long itemId, int price, String itemName)`: itemCache::SimpleKey [1, 10000, Macbook]
- `CacheManager` 스프링 빈에 설정된 `StringRedisSerializer`로  캐시 키는 문자열로 변경되어 저장되고, `Jackson2JsonRedisSerializer`로 캐시 데이터를 JSON 형식으로 변경되어 저장된다.
- `@Cacheable` 사용시 메서드의 인자와 리턴 타입 변경에 유의해야 한다.
  - 운영 중인 시스템의 인자를 추가한다면 캐시 키 값이 변경될 수 있다. → 데이터 저장소에 저장된 데이터를 활용할 수 없게 된다.
  - 메서드의 리턴 타입을 다른 클래스로 변경한다면, 데이터 저장소에 저장된 데이터가 언마셜링되는 과정 중 에러가 발생할 수 있다.

## @Cacheable 미적용

```powershell
# 최초 호출
ItemController 호출 [itemId = 1]
ItemService 호출 [itemId = 1]

# 재호출
ItemController 호출 [itemId = 1]
ItemService 호출 [itemId = 1]
```

## @Cacheable 적용

```powershell
# 최초 호출
ItemController 호출 [itemId = 1]
ItemService 호출 [itemId = 1]

# 재호출
ItemController 호출 [itemId = 1]
```

# @CacheEvict

- **캐시 데이터를 캐시에서 제거**하는 목적으로 사용된다.
- 원본 데이터를 변경하거나 삭제하는 메서드에 적용하면 된다.
  - 원본 데이터가 변경되면 캐시에서 삭제하고, `@Cacheable`이 적용된 메서드가 실행되면 다시 변경된 데이터가 저장된다.

| 속성 | 설명 | Type |
| --- | --- | --- |
| cacheName | 제거할 캐시 이름 | String[] |
| value | cacheName의 별칭(alias) | String[] |
| key | 동적인 키 값을 사용하는 SpEL표현식. 동일한 cacheName을 사용하지만 구분될 필요가 있을때 사용 | String |
| allEntries | 캐시 내의 모든 리소스를 삭제할 지의 여부 | boolean |
| condition | SpEL 표현식이 참인 경우에만 캐싱 적용. or, and 등 조건식 및 논리연산 가능 | String |
| cacheManager | 사용할 CacheManager 지정 | String |
| beforeInvocation | true인 경우 메서드 수행 이전 캐시 리소스 삭제. false인 경우 메서드 수행 후 캐시 리소스 삭제 | boolean |

## 적용 예제

```java
@CacheEvict(value = "itemCache", key = "#itemId")
public Item modifyItem(Long itemId, int price) {
    log.info("ItemService#modifyItem 호출 [itemId = {}, price = {}]", itemId, price);
    return itemRepository.findById(itemId)
        .map(item -> item.modifyPrice(price))
        .orElseThrow(IllegalArgumentException::new);
}
```

# @CachePut

- **캐시를 생성하는 기능만 제공**하는 어노테이션이다.
- `@Cacheable`과 유사하게 실행 결과를 캐시에 저장하지만, **조회시 저장된 캐시의 내용을 사용하지는 않고 항상 메서드의 로직을 실행**한다.

| 속성 | 설명 | Type |
| --- | --- | --- |
| cacheName | 캐시 이름(설정 메서드 리턴값이 저장되는) | String[] |
| value | cacheName의 별칭(alias) | String[] |
| key | 동적인 키 값을 사용하는 SpEL표현식. 동일한 cacheName을 사용하지만 구분될 필요가 있을때 사용 | String |
| condition | SpEL 표현식이 참인 경우에만 캐싱 적용. or, and 등 조건식 및 논리연산 가능 | String |
| unless | 캐싱을 막기 위해 사용되는 SpEL 표현식. condition과 반대로 참일 경우에만 캐싱이 적용되지 않음 | String |

# @Caching

- **두 개 이상의 캐시 어노테이션을 조합하여 사용**한다.

| 속성 | 설명 | Type |
| --- | --- | --- |
| cacheable | 적용될 `@Cacheable` array를 등록 | Cacheable[] |
| evict | 적용될 `@CacheEvict` array를 등록 | CacheEvict[] |
| put | 적용될 `@CachePut` array를 등록 | CachePut[] |

## 적용 예제

```java
@Caching(cacheable = {
    @Cachable(value = "primaryItemCache"),
    @Cachable(value = "secondaryItemCache")
})
public Item searchItem(Long itemId) {
}
```

# @CacheConfig

- **클래스 단위로 캐시 설정**을 동일하게 하고 싶을 때 사용한다.

| 속성 | 설명 | Type |
| --- | --- | --- |
| cacheNames | 해당 클래스 내 정의된 캐시 작업에서의 default 캐시 이름 | String[] |
| cacheManager | 사용할 CacheManager 지정 | String |

## 적용 예제

```java
@Service
@CacheConfig(cacheNames = {"itemCache"})
public class ItemService {

    @Cacheable
    public Item searchItem(Long itemId) {
    }
    
    @CacheEvict(key = "#itemId")
    public Item modifyItem(Long itemId, int price) {
    }
}
```