(()=>{
const $=(s,r=document)=>r.querySelector(s),$$=(s,r=document)=>[...r.querySelectorAll(s)],app=$('#app');
const CATS=['Technology','Programming','Career','Education','Artificial Intelligence','Lifestyle'];
let me=null,csrf='';
const esc=s=>String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const fmt=d=>new Date(d).toLocaleDateString('en-GB',{day:'numeric',month:'short',year:'numeric'});
const fmtT=d=>new Date(d).toLocaleString('en-GB',{day:'numeric',month:'short',hour:'2-digit',minute:'2-digit'});
const body=t=>esc(t).split(/\n{2,}/).map(p=>p.startsWith('## ')?`<h2>${p.slice(3)}</h2>`:`<p>${p.replace(/\n/g,'<br>')}</p>`).join('');
async function api(method,path,b){const h={'Content-Type':'application/json'};if(method!=='GET')h['X-XSRF-TOKEN']=csrf;
 const r=await fetch('/api'+path,{method,headers:h,credentials:'same-origin',body:b?JSON.stringify(b):undefined});let d=null;try{d=await r.json()}catch(e){}
 if(!r.ok){const e=new Error(d&&d.message||'Something went wrong. Try again.');e.status=r.status;throw e}return d}
function toast(m,t){const e=document.createElement('div');e.className='toast '+(t||'');e.textContent=m;$('#toasts').append(e);setTimeout(()=>e.remove(),3500)}
const empty=(m,l='#/explore',t='Browse stories')=>`<div class="empty"><p>${esc(m)}</p><a class="btn" href="${l}">${t}</a></div>`;
const card=(p,c='')=>`<article class="card ${c}"><a class="cover" href="#/post/${p.id}">${p.coverImage?`<img loading="lazy" src="${esc(p.coverImage)}" alt="" onerror="this.remove()">`:''}</a><div class="cbody"><a class="cat" href="#/explore?cat=${encodeURIComponent(p.category)}">${esc(p.category)}</a><h3><a href="#/post/${p.id}">${esc(p.title)}</a></h3><p>${esc(p.description)}</p><div class="meta"><span>${esc(p.authorName)}</span><span>${fmt(p.createdAt)}</span><span>${p.readingTime} min read</span><span>${p.likes} likes</span></div></div></article>`;
function renderNav(){$('#auth').innerHTML=me?`<a data-r="profile" href="#/profile">My Profile</a>${me.role==='ADMIN'?'<a data-r="admin" href="#/admin">Admin</a>':''}<button class="link" id="lo">Log out</button>`:`<a href="#/login">Log in</a><a href="#/register">Register</a>`;
 if(me)$('#lo').onclick=async()=>{await api('POST','/auth/logout');me=null;renderNav();toast('Logged out.');location.hash='#/'}}
$('#burger').onclick=()=>$('#nav').classList.toggle('open');
async function home(){
 const [n,l,a]=await Promise.all([api('GET','/posts?size=7'),api('GET','/posts?sort=liked&size=5'),api('GET','/users/authors')]);const f=l.items[0]||n.items[0];
 app.innerHTML=`<div class="wrap"><div class="hero"><div><h1>Every great idea deserves a story.</h1><p>Discover perspectives, share your knowledge and explore stories that inspire new ways of thinking.</p><div class="acts"><a class="btn" href="#/write">Write a Story</a><a class="btn ghost" href="#/explore">Explore stories</a></div></div>${f?card(f):''}</div>
 ${n.items.length?`<section class="s"><div class="sh"><h2>Latest stories</h2><a href="#/explore">See all</a></div><div class="grid">${n.items.slice(0,6).map((p,i)=>card(p,i?'':'big')).join('')}</div></section>
 <section class="s"><h2>Trending</h2><ol class="trend">${l.items.map((p,i)=>`<li><b>${i+1}</b><div><h3><a href="#/post/${p.id}">${esc(p.title)}</a></h3><div class="meta"><span>${esc(p.authorName)}</span><span>${p.likes} likes</span></div></div></li>`).join('')}</ol></section>`:empty('No stories yet. Be the first to publish.','#/write','Write a Story')}
 <section class="s"><h2>Popular categories</h2><div class="chips">${CATS.map(c=>`<a href="#/explore?cat=${encodeURIComponent(c)}">${c}</a>`).join('')}</div></section>
 ${a.length?`<section class="s"><h2>Featured authors</h2><div class="authors">${a.map(u=>`<a class="author" href="#/profile/${u.id}"><div class="avatar">${esc(u.fullName[0])}</div><h3>${esc(u.fullName)}</h3><p>${esc(u.bio||'')}</p><small>${u.postCount} stories</small></a>`).join('')}</div></section>`:''}</div>
 <section class="news"><div class="wrap"><h2>Get new stories in your inbox</h2><form id="nl"><input type="email" name="email" placeholder="you@example.com" required><button class="btn">Subscribe</button></form></div></section>`;
 $('#nl').onsubmit=async e=>{e.preventDefault();try{const r=await api('POST','/newsletter',{email:e.target.email.value});toast(r.message);e.target.reset()}catch(x){toast(x.message,'err')}}}
async function explore(_,qs){const s={q:qs.get('q')||'',cat:qs.get('cat')||'',sort:qs.get('sort')||'newest',page:0};
 app.innerHTML=`<section class="wrap"><h1 class="page">Explore</h1><form id="f" class="bar"><input name="q" placeholder="Search titles and keywords" value="${esc(s.q)}"><select name="sort"><option value="newest">Newest</option><option value="liked">Most liked</option></select><button class="btn">Search</button></form>
 <div class="chips"><a href="#/explore" class="${s.cat?'':'on'}">All</a>${CATS.map(c=>`<a class="${c===s.cat?'on':''}" href="#/explore?cat=${encodeURIComponent(c)}">${c}</a>`).join('')}</div><div id="res" class="grid"></div><div class="more"><button class="btn ghost" id="more" hidden>Load more</button></div></section>`;
 $('#f select').value=s.sort;
 const load=async()=>{const d=await api('GET',`/posts?q=${encodeURIComponent(s.q)}&category=${encodeURIComponent(s.cat)}&sort=${s.sort}&page=${s.page}&size=9`);
  if(!s.page&&!d.items.length)$('#res').innerHTML='<div class="empty">No stories match. Try different words or another category.</div>';else $('#res').insertAdjacentHTML('beforeend',d.items.map(p=>card(p)).join(''));$('#more').hidden=s.page+1>=d.totalPages};
 $('#f').onsubmit=e=>{e.preventDefault();s.q=e.target.q.value;s.sort=e.target.sort.value;s.page=0;$('#res').innerHTML='';load().catch(x=>toast(x.message,'err'))};
 $('#f select').onchange=()=>$('#f').requestSubmit();$('#more').onclick=()=>{s.page++;load()};await load()}
async function categories(){app.innerHTML=`<section class="wrap"><h1 class="page">Categories</h1><div class="cats">${CATS.map(c=>`<a href="#/explore?cat=${encodeURIComponent(c)}">${c}</a>`).join('')}</div></section>`}
function authForm(reg){
 if(me){location.hash='#/profile';return}
 app.innerHTML=`<div class="formbox"><h1>${reg?'Create your account':'Welcome back'}</h1><form id="af" novalidate>${reg?'<label>Full name</label><input name="fullName" required maxlength="100">':''}<label>Email</label><input name="email" type="email" required>
 <label>Password</label><input name="password" type="password" required minlength="8">${reg?'<label>Confirm password</label><input name="confirmPassword" type="password" required>':''}<div class="err" id="er"></div><button class="btn">${reg?'Create account':'Log in'}</button></form>
 <p>${reg?'Already registered? <a href="#/login"><u>Log in</u></a>':'New here? <a href="#/register"><u>Create an account</u></a>'}</p></div>`;
 $('#af').onsubmit=async e=>{e.preventDefault();const f=Object.fromEntries(new FormData(e.target)),er=$('#er');er.textContent='';
  if(!/^\S+@\S+\.\S+$/.test(f.email))return er.textContent='Enter a valid email address.';if(f.password.length<8)return er.textContent='Password must be at least 8 characters.';
  if(reg&&f.password!==f.confirmPassword)return er.textContent='Passwords do not match.';
  try{if(reg){await api('POST','/auth/register',f);toast('Account created. Log in to continue.');location.hash='#/login'}
   else{me=await api('POST','/auth/login',f);const r=await api('GET','/auth/me');csrf=r.csrf;me=r.user;renderNav();toast('Welcome back, '+me.fullName+'.');location.hash='#/'}}catch(x){er.textContent=x.message}}}
async function post(id){
 const [p,cs]=await Promise.all([api('GET','/posts/'+id),api('GET',`/posts/${id}/comments`)]);
 const rel=(await api('GET',`/posts?category=${encodeURIComponent(p.category)}&size=4`)).items.filter(x=>x.id!==p.id).slice(0,3);
 const can=me&&(me.id===p.authorId||me.role==='ADMIN');
 const cl=l=>l.length?l.map(c=>`<div class="cm"><b>${esc(c.userName)}</b> <small>${fmtT(c.createdAt)}</small><p>${esc(c.content)}</p>${me&&(me.id===c.userId||me.role==='ADMIN')?`<button class="link" data-del="${c.id}">Delete</button>`:''}</div>`).join(''):'<p>No comments yet. Start the conversation.</p>';
 app.innerHTML=`<article class="read"><a class="cat" href="#/explore?cat=${encodeURIComponent(p.category)}">${esc(p.category)}</a><h1>${esc(p.title)}</h1><p class="lead">${esc(p.description)}</p>
 <div class="meta"><a href="#/profile/${p.authorId}">${esc(p.authorName)}</a><span>${fmt(p.createdAt)}</span><span>${p.readingTime} min read</span></div>
 ${p.coverImage?`<img class="hero-img" src="${esc(p.coverImage)}" alt="" onerror="this.remove()">`:''}<div class="body">${body(p.content)}</div>
 <div class="actions"><button class="btn ghost like ${p.liked?'on':''}" id="lk">${p.liked?'Liked':'Like'} (${p.likes})</button>${can?`<a class="btn ghost" href="#/edit/${p.id}">Edit</a><button class="btn danger" id="dp">Delete</button>`:''}</div>
 <section><h2>Comments (<span id="cc">${cs.length}</span>)</h2>${me?'<form id="cf"><textarea name="content" maxlength="1000" placeholder="Add a comment" required></textarea><button class="btn sm" style="margin-top:10px">Post comment</button></form>':'<p><a href="#/login"><u>Log in</u></a> to comment.</p>'}<div id="cl">${cl(cs)}</div></section>
 ${rel.length?`<section><h2>Related stories</h2><div class="grid" style="grid-template-columns:1fr">${rel.map(x=>card(x)).join('')}</div></section>`:''}</article>`;
 $('#lk').onclick=async()=>{if(!me){location.hash='#/login';return}try{const u=await api(p.liked?'DELETE':'POST',`/posts/${id}/like`);p.liked=u.liked;p.likes=u.likes;const b=$('#lk');b.textContent=`${u.liked?'Liked':'Like'} (${u.likes})`;b.classList.toggle('on',u.liked)}catch(x){toast(x.message,'err')}};
 const dp=$('#dp');if(dp)dp.onclick=async()=>{if(!confirm('Delete this story permanently?'))return;try{await api('DELETE','/posts/'+id);toast('Story deleted.');location.hash='#/explore'}catch(x){toast(x.message,'err')}};
 const bind=()=>$$('[data-del]').forEach(b=>b.onclick=async()=>{if(!confirm('Delete this comment?'))return;try{await api('DELETE','/comments/'+b.dataset.del);const l=await api('GET',`/posts/${id}/comments`);$('#cl').innerHTML=cl(l);$('#cc').textContent=l.length;bind();toast('Comment deleted.')}catch(x){toast(x.message,'err')}});bind();
 const cf=$('#cf');if(cf)cf.onsubmit=async e=>{e.preventDefault();try{await api('POST',`/posts/${id}/comments`,{content:cf.content.value});const l=await api('GET',`/posts/${id}/comments`);cf.reset();$('#cl').innerHTML=cl(l);$('#cc').textContent=l.length;bind();toast('Comment posted.')}catch(x){toast(x.message,'err')}}}
async function editor(id){
 if(!me){location.hash='#/login';return}const p=id?await api('GET','/posts/'+id):{title:'',description:'',content:'',coverImage:'',category:CATS[0]};
 app.innerHTML=`<form class="editor" id="ef"><input class="t" name="title" placeholder="Title" maxlength="200" required value="${esc(p.title)}"><label>Description</label><input name="description" maxlength="500" required value="${esc(p.description)}">
 <label>Category</label><select name="category">${CATS.map(c=>`<option ${c===p.category?'selected':''}>${c}</option>`).join('')}</select><label>Cover image URL (https)</label><input name="coverImage" type="url" value="${esc(p.coverImage||'')}">
 <label>Story</label><textarea name="content" required>${esc(p.content)}</textarea><div id="pv" class="body" hidden></div><div class="err" id="er"></div>
 <div class="actions"><button class="btn">${id?'Save changes':'Publish'}</button><button type="button" class="btn ghost" id="pb">Preview</button></div></form>`;
 $('#pb').onclick=()=>{const v=$('#pv'),t=$('[name=content]'),on=v.hidden;v.hidden=!on;t.hidden=on;if(on)v.innerHTML=`<h1>${esc($('[name=title]').value)}</h1>`+body(t.value);$('#pb').textContent=on?'Edit':'Preview'};
 $('#ef').onsubmit=async e=>{e.preventDefault();const f=Object.fromEntries(new FormData(e.target));try{const r=await api(id?'PUT':'POST',id?'/posts/'+id:'/posts',f);toast(id?'Changes saved.':'Published.');location.hash='#/post/'+r.id}catch(x){$('#er').textContent=x.message}}}
async function profile(id){id=id||(me&&me.id);if(!id){location.hash='#/login';return}
 const [u,ps]=await Promise.all([api('GET','/users/'+id),api('GET',`/users/${id}/posts`)]);const own=me&&me.id===u.id;
 app.innerHTML=`<section class="wrap"><div class="page"><div class="avatar">${esc(u.fullName[0])}</div><h1>${esc(u.fullName)}</h1><p>${esc(u.email)}<br>Joined ${fmt(u.createdAt)}. ${u.postCount} published ${u.postCount===1?'story':'stories'}.</p><p>${esc(u.bio||'No bio yet.')}</p></div>
 ${own?`<form id="pf" style="max-width:520px"><label>Bio</label><textarea name="bio" maxlength="500" rows="3">${esc(u.bio||'')}</textarea><button class="btn sm" style="margin-top:10px">Save bio</button></form>`:''}
 <h2 style="margin-top:48px">Stories</h2><div class="grid">${ps.length?ps.map(p=>card(p)).join(''):'<div class="empty">No published stories yet.</div>'}</div></section>`;
 const pf=$('#pf');if(pf)pf.onsubmit=async e=>{e.preventDefault();try{me=await api('PUT','/users/profile',{bio:pf.bio.value});toast('Profile updated.');profile(id)}catch(x){toast(x.message,'err')}}}
async function admin(_,__,tab='overview'){
 if(!me||me.role!=='ADMIN'){app.innerHTML=empty('Administrators only.','#/','Back home');return}
 const tabs=['overview','users','stories','comments'];let h='';
 if(tab==='overview'){const d=await api('GET','/admin/dashboard');h=`<div class="stats">${['users','posts','comments','likes'].map(k=>`<div class="stat"><b>${d[k]}</b>${k==='posts'?'published blogs':k}</div>`).join('')}</div><h2>Recent activity</h2>${d.recent.map(r=>`<div class="cm">${esc(r.text)} <small>${fmtT(r.at)}</small></div>`).join('')}`}
 if(tab==='users'){const l=await api('GET','/admin/users');h=`<div class="tw"><table><tr><th>Name</th><th>Email</th><th>Role</th><th>Stories</th><th></th></tr>${l.map(u=>`<tr><td>${esc(u.fullName)}</td><td>${esc(u.email)}</td><td>${u.role}</td><td>${u.postCount}</td><td>${u.id===me.id?'':`<button class="link" data-a="/admin/users/${u.id}" data-m="Delete this user and all their content?">Delete</button>`}</td></tr>`).join('')}</table></div>`}
 if(tab==='stories'){const l=await api('GET','/admin/posts');h=`<div class="tw"><table><tr><th>Title</th><th>Author</th><th>Category</th><th>Likes</th><th></th></tr>${l.map(p=>`<tr><td><a href="#/post/${p.id}">${esc(p.title)}</a></td><td>${esc(p.authorName)}</td><td>${esc(p.category)}</td><td>${p.likes}</td><td><button class="link" data-a="/admin/posts/${p.id}" data-m="Delete this story?">Delete</button></td></tr>`).join('')}</table></div>`}
 if(tab==='comments'){const l=await api('GET','/admin/comments');h=l.length?`<div class="tw"><table><tr><th>Comment</th><th>By</th><th>When</th><th></th></tr>${l.map(c=>`<tr><td>${esc(c.content)}</td><td>${esc(c.userName)}</td><td>${fmtT(c.createdAt)}</td><td><button class="link" data-a="/comments/${c.id}" data-m="Delete this comment?">Delete</button></td></tr>`).join('')}</table></div>`:'<p>No comments to moderate.</p>'}
 app.innerHTML=`<section class="wrap"><h1 class="page">Admin dashboard</h1><div class="tabs">${tabs.map(t=>`<button data-t="${t}" class="${t===tab?'on':''}">${t[0].toUpperCase()+t.slice(1)}</button>`).join('')}</div>${h}</section>`;
 $$('[data-t]').forEach(b=>b.onclick=()=>admin(0,0,b.dataset.t).catch(x=>toast(x.message,'err')));
 $$('[data-a]').forEach(b=>b.onclick=async()=>{if(!confirm(b.dataset.m))return;try{await api('DELETE',b.dataset.a);toast('Deleted.');admin(0,0,tab)}catch(x){toast(x.message,'err')}})}
const routes={'':home,explore,categories,login:()=>authForm(false),register:()=>authForm(true),write:()=>editor(),edit:id=>editor(id),post,profile,admin};
async function route(){const [path,qs]=location.hash.slice(2).split('?'),[name,arg]=path.split('/'),q=new URLSearchParams(qs||'');
 $('#nav').classList.remove('open');window.scrollTo(0,0);$$('#nav a[data-r]').forEach(a=>a.classList.toggle('on',a.dataset.r===name));
 app.innerHTML='<div class="loading">Loading…</div>';
 try{await(routes[name]||home)(arg,q)}catch(e){if(e.status===401){location.hash='#/login';toast('Log in to continue.','err')}else app.innerHTML=empty(e.message,'#/','Back home')}}
addEventListener('hashchange',route);
(async()=>{try{const r=await api('GET','/auth/me');csrf=r.csrf;me=r.user}catch(e){}renderNav();route()})();
})();
