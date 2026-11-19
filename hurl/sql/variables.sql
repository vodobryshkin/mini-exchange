SELECT 'hurl_simple_buyer_' || :case_no AS buyer_username,
       'hurl_simple_seller_' || :case_no AS seller_username,
       'HURL_SIMPLE_' || :case_no AS sec_code,
       'HURL_SIMPLE_' || :case_no || '_B' AS second_code,
       'HURL_SIMPLE_' || :case_no || '_E' AS empty_code,
       'HURL_SIMPLE_' || :case_no || '_D' AS day_code,
       'HURL_SIMPLE_' || :case_no || '_R' AS reverse_code,
       'HURL_MISSING_' || :case_no AS missing_code,
       CASE WHEN :case_no = 1 THEN 10 ELSE 1 END AS lot_size
\gset
\echo buyer_username=:buyer_username
\echo seller_username=:seller_username
\echo sec_code=:sec_code
\echo second_code=:second_code
\echo empty_code=:empty_code
\echo day_code=:day_code
\echo reverse_code=:reverse_code
\echo missing_code=:missing_code
