---
title: "Creating Tables"
weight: 2
type: docs
aliases:
- /how-to/creating-tables.html
---
<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Creating Tables

## Creating Catalog Managed Tables

Tables created in Table Store [catalogs]({{< ref "docs/how-to/creating-catalogs" >}}) are managed by the catalog. When the table is dropped from catalog, its table files will also be deleted.

The following SQL assumes that you have registered and are using a Table Store catalog. It creates a managed table named `MyTable` with five columns in the catalog's `default` database, where `dt`, `hh` and `user_id` are the primary keys.

{{< tabs "primary-keys-example" >}}

{{< tab "Flink" >}}

```sql
CREATE TABLE MyTable (
    user_id BIGINT,
    item_id BIGINT,
    behavior STRING,
    dt STRING,
    hh STRING,
    PRIMARY KEY (dt, hh, user_id) NOT ENFORCED
);
```

{{< /tab >}}

{{< tab "Spark3" >}}

```sql
CREATE TABLE MyTable (
    user_id BIGINT,
    item_id BIGINT,
    behavior STRING,
    dt STRING,
    hh STRING
) TBLPROPERTIES (
    'primary-key' = 'dt,hh,user_id'
);
```

{{< /tab >}}

{{< /tabs >}}

### Partitioned Tables

The following SQL creates a table named `MyTable` with five columns partitioned by `dt` and `hh`, where `dt`, `hh` and `user_id` are the primary keys.

{{< tabs "partitions-example" >}}

{{< tab "Flink" >}}

```sql
CREATE TABLE MyTable (
    user_id BIGINT,
    item_id BIGINT,
    behavior STRING,
    dt STRING,
    hh STRING,
    PRIMARY KEY (dt, hh, user_id) NOT ENFORCED
) PARTITIONED BY (dt, hh);
```

{{< /tab >}}

{{< tab "Spark3" >}}

```sql
CREATE TABLE MyTable (
    user_id BIGINT,
    item_id BIGINT,
    behavior STRING,
    dt STRING,
    hh STRING
) PARTITIONED BY (dt, hh) TBLPROPERTIES (
    'primary-key' = 'dt,hh,user_id'
);
```

{{< /tab >}}

{{< /tabs >}}

{{< hint info >}}

Partition keys must be a subset of primary keys if primary keys are defined.

{{< /hint >}}

### Table Properties

Users can specify table properties to enable features or improve performance of Table Store. For a complete list of such properties, see [configurations]({{< ref "docs/maintenance/configurations" >}}).

The following SQL creates a table named `MyTable` with five columns partitioned by `dt` and `hh`, where `dt`, `hh` and `user_id` are the primary keys. This table has two properties: `'bucket' = '2'` and `'bucket-key' = 'user_id'`.

{{< tabs "table-properties-example" >}}

{{< tab "Flink" >}}

```sql
CREATE TABLE MyTable (
    user_id BIGINT,
    item_id BIGINT,
    behavior STRING,
    dt STRING,
    hh STRING,
    PRIMARY KEY (dt, hh, user_id) NOT ENFORCED
) PARTITIONED BY (dt, hh) WITH (
    'bucket' = '2',
    'bucket-key' = 'user_id'
);
```

{{< /tab >}}

{{< tab "Spark3" >}}

```sql
CREATE TABLE MyTable (
    user_id BIGINT,
    item_id BIGINT,
    behavior STRING,
    dt STRING,
    hh STRING
) PARTITIONED BY (dt, hh) TBLPROPERTIES (
    'primary-key' = 'dt,hh,user_id',
    'bucket' = '2',
    'bucket-key' = 'user_id'
);
```

{{< /tab >}}

{{< /tabs >}}

## Creating External Tables

External tables are recorded but not managed by catalogs. If an external table is dropped, its table files will not be deleted.

Table Store external tables can be used in any catalog. If you do not want to create a Table Store catalog and just want to read / write a table, you can consider external tables.

{{< tabs "external-table-example" >}}

{{< tab "Flink" >}}

Flink SQL supports reading and writing an external table. External Table Store tables are created by specifying the `connector` and `path` table properties. The following SQL creates an external table named `MyTable` with five columns, where the base path of table files is `hdfs://path/to/table`.

```sql
CREATE TABLE MyTable (
    user_id BIGINT,
    item_id BIGINT,
    behavior STRING,
    dt STRING,
    hh STRING,
    PRIMARY KEY (dt, hh, user_id) NOT ENFORCED
) WITH (
    'connector' = 'table-store',
    'path' = 'hdfs://path/to/table',
    'auto-create' = 'true' -- this table property creates table files for an empty table if table path does not exist
                           -- currently only supported by Flink
);
```

{{< /tab >}}

{{< tab "Spark3" >}}

Spark3 only supports creating external tables through Scala API. The following Scala code loads the table located at `hdfs://path/to/table` into a `DataSet`.

```scala
val dataset = spark.read.format("tablestore").load("hdfs://path/to/table")
```

{{< /tab >}}

{{< tab "Spark2" >}}

Spark2 only supports creating external tables through Scala API. The following Scala code loads the table located at `hdfs://path/to/table` into a `DataSet`.

```scala
val dataset = spark.read.format("tablestore").load("hdfs://path/to/table")
```

{{< /tab >}}

{{< tab "Hive" >}}

Hive SQL only supports reading from an external table. The following SQL creates an external table named `my_table`, where the base path of table files is `hdfs://path/to/table`. As schemas are stored in table files, users do not need to write column definitions.

```sql
CREATE EXTERNAL TABLE my_table
STORED BY 'org.apache.flink.table.store.hive.TableStoreHiveStorageHandler'
LOCATION 'hdfs://path/to/table';
```

{{< /tab >}}

{{< /tabs >}}

## Creating Temporary Tables

{{< tabs "temporary-table-example" >}}

{{< tab "Flink" >}}

Temporary tables are only supported by Flink. Like external tables, temporary tables are just recorded but not managed by the current Flink SQL session. If the temporary table is dropped, its resources will not be deleted. Temporary tables are also dropped when Flink SQL session is closed.

If you want to use Table Store catalog along with other tables but do not want to store them in other catalogs, you can create a temporary table. The following Flink SQL creates a Table Store catalog and a temporary table and also illustrates how to use both tables together.

```sql
CREATE CATALOG my_catalog WITH (
    'type' = 'table-store',
    'warehouse' = 'hdfs://path/to/warehouse'
);

USE CATALOG my_catalog;

-- Assume that there is already a table named my_table in my_catalog

CREATE TEMPORARY TABLE temp_table (
    k INT,
    v STRING
) WITH (
    'connector' = 'filesystem',
    'path' = 'hdfs://path/to/temp_table.csv',
    'format' = 'csv'
);

SELECT my_table.k, my_table.v, temp_table.v FROM my_table JOIN temp_table ON my_table.k = temp_table.k;
```

{{< /tab >}}

{{< /tabs >}}
